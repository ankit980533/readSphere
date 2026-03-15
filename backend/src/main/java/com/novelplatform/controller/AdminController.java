package com.novelplatform.controller;

import com.novelplatform.dto.NovelRequest;
import com.novelplatform.model.Novel;
import com.novelplatform.service.NovelService;
import com.novelplatform.service.PdfProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private NovelService novelService;
    
    @Autowired
    private PdfProcessingService pdfProcessingService;
    
    @PostMapping("/novels")
    public ResponseEntity<Novel> createNovel(@RequestBody NovelRequest request) {
        Novel novel = novelService.createNovel(request);
        return ResponseEntity.ok(novel);
    }
    
    @PostMapping("/upload-pdf")
    public ResponseEntity<?> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("genreId") Long genreId,
            @RequestParam("description") String description) {
        
        try {
            Novel novel = pdfProcessingService.processPdfUpload(file, title, author, genreId, description);
            return ResponseEntity.ok(novel);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing PDF: " + e.getMessage());
        }
    }
    
    @PutMapping("/novels/{id}/status")
    public ResponseEntity<Novel> updateNovelStatus(
            @PathVariable Long id, 
            @RequestParam Novel.Status status) {
        Novel novel = novelService.updateStatus(id, status);
        return ResponseEntity.ok(novel);
    }
}
