package com.novelplatform.service;

import com.novelplatform.model.Chapter;
import com.novelplatform.model.Novel;
import com.novelplatform.model.ReadingHistory;
import com.novelplatform.model.User;
import com.novelplatform.repository.ChapterRepository;
import com.novelplatform.repository.NovelRepository;
import com.novelplatform.repository.ReadingHistoryRepository;
import com.novelplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReadingHistoryService {
    
    @Autowired
    private ReadingHistoryRepository historyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    public List<ReadingHistory> getUserHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return historyRepository.findByUserIdOrderByLastReadAtDesc(user.getId());
    }
    
    public ReadingHistory updateHistory(String email, Long novelId, Long chapterId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        
        ReadingHistory history = historyRepository.findByUserIdAndNovelId(user.getId(), novelId)
                .orElse(new ReadingHistory());
        
        history.setUser(user);
        history.setNovel(novel);
        history.setChapter(chapter);
        history.setLastReadAt(LocalDateTime.now());
        
        return historyRepository.save(history);
    }
}
