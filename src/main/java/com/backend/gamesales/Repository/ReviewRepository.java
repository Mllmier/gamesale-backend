package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review,Long> {
    boolean existsByGameIdAndUserId(Long gameId, Long userId);
}
