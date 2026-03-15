package com.novelplatform.controller;

import com.novelplatform.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasRole('ADMIN')")
public class AiController {
    
    @Autowired
    private AiService aiService;
    
    @PostMapping("/detect-chapters")
    public List<AiService.ChapterDetectionResult> detectChapters(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return aiService.detectChapters(text);
    }
    
    @PostMapping("/detect-genre")
    public Map<String, String> detectGenre(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String description = request.get("description");
        String sampleText = request.get("sampleText");
        
        String genre = aiService.detectGenre(title, description, sampleText);
        return Map.of("genre", genre);
    }
    
    @PostMapping("/generate-summary")
    public Map<String, String> generateSummary(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String summary = aiService.generateSummary(text);
        return Map.of("summary", summary);
    }
    
    @PostMapping("/moderate-content")
    public AiService.ContentModerationResult moderateContent(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return aiService.moderateContent(text);
    }
}
