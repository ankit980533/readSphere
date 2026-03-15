package com.novelplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

/**
 * Free local AI service using Ollama
 * No API key required, runs on your machine
 */
@Service
public class OllamaAiService {
    
    @Value("${ollama.api.url:http://localhost:11434}")
    private String ollamaUrl;
    
    @Value("${ollama.model:llama3.2}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public List<AiService.ChapterDetectionResult> detectChapters(String text) {
        String prompt = """
            Analyze this novel text and detect all chapters.
            Return ONLY a JSON array with this exact structure:
            [{"chapter_number":1,"title":"Chapter Title","start_position":0,"end_position":1500}]
            
            Text:
            %s
            """.formatted(text.substring(0, Math.min(text.length(), 8000)));
        
        try {
            String response = callOllama(prompt);
            return parseChapterDetection(response, text);
        } catch (Exception e) {
            return fallbackChapterDetection(text);
        }
    }
    
    public String detectGenre(String title, String description, String sampleText) {
        String prompt = """
            Based on this novel information, determine the primary genre.
            Choose ONE from: Romance, Fantasy, Mystery, Thriller, Horror, Sci-Fi, Adventure, Historical
            
            Title: %s
            Description: %s
            Sample: %s
            
            Respond with ONLY the genre name, nothing else.
            """.formatted(title, description, sampleText.substring(0, Math.min(sampleText.length(), 1000)));
        
        try {
            String response = callOllama(prompt).trim();
            return extractGenre(response);
        } catch (Exception e) {
            return "Fiction";
        }
    }
    
    public String generateSummary(String text) {
        String prompt = """
            Write a compelling 2-3 sentence summary of this novel that would attract readers.
            Focus on the main plot and hook. Be concise.
            
            Text:
            %s
            """.formatted(text.substring(0, Math.min(text.length(), 5000)));
        
        try {
            return callOllama(prompt).trim();
        } catch (Exception e) {
            return "An engaging story that will captivate readers.";
        }
    }
    
    public AiService.ContentModerationResult moderateContent(String text) {
        String prompt = """
            Analyze this text for inappropriate content.
            Check for: explicit violence, hate speech, spam, adult content.
            
            Respond in JSON format:
            {"is_appropriate":true,"issues":[],"severity":"low"}
            
            Text:
            %s
            """.formatted(text.substring(0, Math.min(text.length(), 3000)));
        
        try {
            String response = callOllama(prompt);
            return parseModerationResult(response);
        } catch (Exception e) {
            return new AiService.ContentModerationResult(true, Collections.emptyList(), "low");
        }
    }
    
    private String callOllama(String prompt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);
        requestBody.put("options", Map.of("temperature", 0.3));
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(
            ollamaUrl + "/api/generate",
            HttpMethod.POST,
            request,
            String.class
        );
        
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("response").asText();
    }
    
    private List<AiService.ChapterDetectionResult> parseChapterDetection(String aiResponse, String fullText) {
        try {
            String jsonPart = extractJson(aiResponse);
            JsonNode chapters = objectMapper.readTree(jsonPart);
            List<AiService.ChapterDetectionResult> results = new ArrayList<>();
            
            for (JsonNode chapter : chapters) {
                results.add(new AiService.ChapterDetectionResult(
                    chapter.path("chapter_number").asInt(),
                    chapter.path("title").asText(),
                    chapter.path("start_position").asInt(),
                    chapter.path("end_position").asInt()
                ));
            }
            return results.isEmpty() ? fallbackChapterDetection(fullText) : results;
        } catch (Exception e) {
            return fallbackChapterDetection(fullText);
        }
    }
    
    private String extractJson(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start != -1 && end != -1) {
            return text.substring(start, end + 1);
        }
        return text;
    }
    
    private String extractGenre(String response) {
        String[] validGenres = {"Romance", "Fantasy", "Mystery", "Thriller", "Horror", "Sci-Fi", "Adventure", "Historical"};
        for (String genre : validGenres) {
            if (response.contains(genre)) {
                return genre;
            }
        }
        return "Fiction";
    }
    
    private AiService.ContentModerationResult parseModerationResult(String aiResponse) {
        try {
            String jsonPart = extractJson(aiResponse);
            if (jsonPart.startsWith("{")) {
                JsonNode result = objectMapper.readTree(jsonPart);
                return new AiService.ContentModerationResult(
                    result.path("is_appropriate").asBoolean(true),
                    parseIssues(result.path("issues")),
                    result.path("severity").asText("low")
                );
            }
        } catch (Exception e) {
            // Fallback
        }
        return new AiService.ContentModerationResult(true, Collections.emptyList(), "low");
    }
    
    private List<String> parseIssues(JsonNode issuesNode) {
        List<String> issues = new ArrayList<>();
        if (issuesNode.isArray()) {
            issuesNode.forEach(node -> issues.add(node.asText()));
        }
        return issues;
    }
    
    private List<AiService.ChapterDetectionResult> fallbackChapterDetection(String text) {
        List<AiService.ChapterDetectionResult> results = new ArrayList<>();
        String[] lines = text.split("\n");
        int currentPos = 0;
        int chapterNum = 1;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.matches("(?i)^(Chapter|CHAPTER)\\s+\\d+.*")) {
                int nextChapterPos = findNextChapter(lines, i + 1);
                results.add(new AiService.ChapterDetectionResult(
                    chapterNum++, line, currentPos, nextChapterPos
                ));
                currentPos = nextChapterPos;
            }
        }
        
        if (results.isEmpty()) {
            results.add(new AiService.ChapterDetectionResult(1, "Chapter 1", 0, text.length()));
        }
        
        return results;
    }
    
    private int findNextChapter(String[] lines, int startIndex) {
        int pos = 0;
        for (int i = 0; i < startIndex && i < lines.length; i++) {
            pos += lines[i].length() + 1;
        }
        
        for (int i = startIndex; i < lines.length; i++) {
            if (lines[i].trim().matches("(?i)^(Chapter|CHAPTER)\\s+\\d+.*")) {
                return pos;
            }
            pos += lines[i].length() + 1;
        }
        
        return pos;
    }
}
