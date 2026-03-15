package com.novelplatform.controller;

import com.novelplatform.model.ReadingHistory;
import com.novelplatform.service.ReadingHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class ReadingHistoryController {
    
    @Autowired
    private ReadingHistoryService historyService;
    
    @GetMapping
    public List<ReadingHistory> getUserHistory(Authentication authentication) {
        String email = authentication.getName();
        return historyService.getUserHistory(email);
    }
    
    @PostMapping
    public ReadingHistory updateHistory(@RequestParam Long novelId,
                                       @RequestParam Long chapterId,
                                       Authentication authentication) {
        String email = authentication.getName();
        return historyService.updateHistory(email, novelId, chapterId);
    }
}
