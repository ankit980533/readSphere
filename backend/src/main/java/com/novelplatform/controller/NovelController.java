package com.novelplatform.controller;

import com.novelplatform.model.Novel;
import com.novelplatform.repository.NovelRepository;
import com.novelplatform.repository.ChapterRepository;
import com.novelplatform.service.PdfProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/novels")
public class NovelController {
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @Autowired
    private PdfProcessingService pdfProcessingService;
    
    @GetMapping
    public List<Map<String, Object>> getAllNovels() {
        List<Novel> novels = novelRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Novel novel : novels) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", novel.getId());
            map.put("title", novel.getTitle());
            map.put("author", novel.getAuthor() != null ? novel.getAuthor().getName() : "Unknown");
            map.put("description", novel.getDescription());
            map.put("summary", novel.getSummary());
            map.put("genre", novel.getGenre() != null ? novel.getGenre().getName() : "Unknown");
            map.put("coverImage", novel.getCoverImage());
            map.put("status", novel.getStatus());
            map.put("chapterCount", chapterRepository.countByNovelId(novel.getId()));
            result.add(map);
        }
        return result;
    }
    
    @GetMapping("/{id}")
    public Map<String, Object> getNovel(@PathVariable Long id) {
        Novel novel = novelRepository.findById(id).orElseThrow();
        Map<String, Object> map = new HashMap<>();
        map.put("id", novel.getId());
        map.put("title", novel.getTitle());
        map.put("author", novel.getAuthor() != null ? novel.getAuthor().getName() : "Unknown");
        map.put("description", novel.getDescription());
        map.put("summary", novel.getSummary());
        map.put("genre", novel.getGenre() != null ? novel.getGenre().getName() : "Unknown");
        map.put("coverImage", novel.getCoverImage());
        map.put("status", novel.getStatus());
        map.put("chapterCount", chapterRepository.countByNovelId(novel.getId()));
        return map;
    }
    
    @GetMapping("/genre/{genreId}")
    public List<Novel> getNovelsByGenre(@PathVariable Long genreId) {
        return novelRepository.findByGenreId(genreId);
    }
    
    @GetMapping("/search")
    public List<Novel> searchNovels(@RequestParam String query) {
        return novelRepository.findByTitleContainingIgnoreCase(query);
    }
    
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadNovel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "description", required = false) String description) {
        try {
            Novel novel = pdfProcessingService.processNovelPdf(file, title, author, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", novel.getId());
            response.put("title", novel.getTitle());
            response.put("author", novel.getAuthor() != null ? novel.getAuthor().getName() : author);
            response.put("genre", novel.getGenre() != null ? novel.getGenre().getName() : "Unknown");
            response.put("summary", novel.getSummary());
            response.put("chapterCount", novel.getChapters() != null ? novel.getChapters().size() : 0);
            response.put("message", "Novel uploaded and processed successfully");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Upload failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
