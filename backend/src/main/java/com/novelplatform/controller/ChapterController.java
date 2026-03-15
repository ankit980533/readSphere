package com.novelplatform.controller;

import com.novelplatform.model.Chapter;
import com.novelplatform.repository.ChapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChapterController {
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    @GetMapping("/novels/{novelId}/chapters")
    public List<Chapter> getChapters(@PathVariable Long novelId) {
        return chapterRepository.findByNovelIdOrderByChapterNumber(novelId);
    }
    
    @GetMapping("/chapters/{id}")
    public Chapter getChapter(@PathVariable Long id) {
        return chapterRepository.findById(id).orElseThrow();
    }
}
