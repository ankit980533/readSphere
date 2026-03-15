package com.novelplatform.repository;

import com.novelplatform.model.ReadingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistory, Long> {
    List<ReadingHistory> findByUserIdOrderByLastReadAtDesc(Long userId);
    Optional<ReadingHistory> findByUserIdAndNovelId(Long userId, Long novelId);
}
