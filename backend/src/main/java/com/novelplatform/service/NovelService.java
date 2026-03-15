package com.novelplatform.service;

import com.novelplatform.dto.NovelRequest;
import com.novelplatform.model.Genre;
import com.novelplatform.model.Novel;
import com.novelplatform.model.User;
import com.novelplatform.repository.GenreRepository;
import com.novelplatform.repository.NovelRepository;
import com.novelplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class NovelService {
    
    @Autowired
    private NovelRepository novelRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GenreRepository genreRepository;
    
    public Novel createNovel(NovelRequest request) {
        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new RuntimeException("Genre not found"));
        
        Novel novel = new Novel();
        novel.setTitle(request.getTitle());
        novel.setDescription(request.getDescription());
        novel.setAuthor(author);
        novel.setGenre(genre);
        novel.setCoverImage(request.getCoverImage());
        novel.setSourceType(Novel.SourceType.ADMIN);
        novel.setStatus(Novel.Status.DRAFT);
        novel.setCreatedAt(LocalDateTime.now());
        
        return novelRepository.save(novel);
    }
    
    public Novel updateStatus(Long id, Novel.Status status) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Novel not found"));
        novel.setStatus(status);
        return novelRepository.save(novel);
    }
}
