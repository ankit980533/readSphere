package com.novelplatform.service;

import com.novelplatform.model.Bookmark;
import com.novelplatform.model.Chapter;
import com.novelplatform.model.Novel;
import com.novelplatform.model.User;
import com.novelplatform.repository.BookmarkRepository;
import com.novelplatform.repository.ChapterRepository;
import com.novelplatform.repository.NovelRepository;
import com.novelplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookmarkService {
    
    @Autowired
    private BookmarkRepository bookmarkRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private ChapterRepository chapterRepository;
    
    public List<Bookmark> getUserBookmarks(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return bookmarkRepository.findByUserId(user.getId());
    }
    
    public Bookmark addBookmark(String email, Long novelId, Long chapterId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));
        
        Bookmark bookmark = new Bookmark();
        bookmark.setUser(user);
        bookmark.setNovel(novel);
        bookmark.setChapter(chapter);
        bookmark.setCreatedAt(LocalDateTime.now());
        
        return bookmarkRepository.save(bookmark);
    }
    
    public void removeBookmark(Long id) {
        bookmarkRepository.deleteById(id);
    }
}
