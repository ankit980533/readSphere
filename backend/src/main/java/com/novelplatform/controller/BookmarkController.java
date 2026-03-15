package com.novelplatform.controller;

import com.novelplatform.model.Bookmark;
import com.novelplatform.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    
    @Autowired
    private BookmarkService bookmarkService;
    
    @GetMapping
    public List<Bookmark> getUserBookmarks(Authentication authentication) {
        String email = authentication.getName();
        return bookmarkService.getUserBookmarks(email);
    }
    
    @PostMapping
    public Bookmark addBookmark(@RequestParam Long novelId, 
                                @RequestParam Long chapterId,
                                Authentication authentication) {
        String email = authentication.getName();
        return bookmarkService.addBookmark(email, novelId, chapterId);
    }
    
    @DeleteMapping("/{id}")
    public void removeBookmark(@PathVariable Long id) {
        bookmarkService.removeBookmark(id);
    }
}
