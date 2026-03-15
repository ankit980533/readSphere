package com.novelplatform.controller;

import com.novelplatform.dto.NovelRequest;
import com.novelplatform.model.Novel;
import com.novelplatform.service.NovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/author")
@PreAuthorize("hasRole('AUTHOR')")
public class AuthorController {
    
    @Autowired
    private NovelService novelService;
    
    @PostMapping("/novels")
    public Novel createNovel(@RequestBody NovelRequest request) {
        return novelService.createNovel(request);
    }
}
