package com.backend.gamesales.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.gamesales.Model.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByGameIdAndUserId(Long gameId, Long userId);
    java.util.List<Review> findByGameIdOrderByCreatedAtDesc(Long gameId);
}
