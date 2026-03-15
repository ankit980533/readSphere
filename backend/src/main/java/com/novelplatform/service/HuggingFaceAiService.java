package com.novelplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

/**
 * Free AI service using Hugging Face Inference API
 * Free tier: 30,000 requests/month
 */
@Service
public class HuggingFaceAiService {
    
    @Value("${huggingface.api.key:}")
    private String apiKey;
    
    @Value("${huggingface.api.url:https://api-inference.huggingface.co/models}")
    private String apiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Free models
    private static final String TEXT_GENERATION_MODEL = "mistralai/Mistral-7B-Instruct-v0.2";
    private static final String CLASSIFICATION_MODEL = "facebook/bart-large-mnli";
    
    public String detectGenre(String title, String description, String sampleText) {
        String text = title + " " + description + " " + sampleText.substring(0, Math.min(500, sampleText.length()));
        
        try {
            String[] genres = {"Romance", "Fantasy", "Mystery", "Thriller", "Horror", "Science Fiction", "Adventure", "Historical"};
            return classifyText(text, genres);
        } catch (Exception e) {
            return "Fiction";
        }
    }
    
    public String generateSummary(String text) {
        String prompt = "Summarize this novel in 2-3 sentences: " + text.substring(0, Math.min(2000, text.length()));
        
        try {
            return generateText(prompt, 100);
        } catch (Exception e) {
            return "An engaging story that will captivate readers.";
        }
    }
    
    public AiService.ContentModerationResult moderateContent(String text) {
        try {
            String[] categories = {"appropriate content", "inappropriate content", "spam", "hate speech"};
            String result = classifyText(text.substring(0, Math.min(1000, text.length())), categories);
            
            boolean isAppropriate = result.equals("appropriate content");
            List<String> issues = isAppropriate ? Collections.emptyList() : List.of(result);
            String severity = isAppropriate ? "low" : "high";
            
            return new AiService.ContentModerationResult(isAppropriate, issues, severity);
        } catch (Exception e) {
            return new AiService.ContentModerationResult(true, Collections.emptyList(), "low");
        }
    }
    
    private String generateText(String prompt, int maxLength) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", prompt);
        requestBody.put("parameters", Map.of(
            "max_length", maxLength,
            "temperature", 0.7,
            "return_full_text", false
        ));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl + "/" + TEXT_GENERATION_MODEL,
            HttpMethod.POST,
            request,
            String.class
        );
        
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.get(0).path("generated_text").asText().trim();
    }
    
    private String classifyText(String text, String[] candidateLabels) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", text);
        requestBody.put("parameters", Map.of("candidate_labels", candidateLabels));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl + "/" + CLASSIFICATION_MODEL,
            HttpMethod.POST,
            request,
            String.class
        );
        
        JsonNode root = objectMapper.readTree(response.getBody());
        JsonNode labels = root.path("labels");
        return labels.get(0).asText();
    }
}
