package com.novelplatform.repository;

import com.novelplatform.model.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NovelRepository extends JpaRepository<Novel, Long> {
    List<Novel> findByStatus(Novel.Status status);
    List<Novel> findByGenreId(Long genreId);
    List<Novel> findByTitleContainingIgnoreCase(String title);
}
