package com.novelplatform.repository;

import com.novelplatform.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByNovelIdOrderByChapterNumber(Long novelId);
    List<Chapter> findByNovelIdOrderByChapterNumberAsc(Long novelId);
    long countByNovelId(Long novelId);
}
