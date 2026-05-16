package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Enums.PromotionStatus;
import com.backend.gamesales.Model.Enums.RequestStatus;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    List<Promotion> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            PromotionStatus status,
            LocalDateTime start,
            LocalDateTime end
    );
    void deleteByGameId(Long gameId);

    boolean existsByGameIdAndStatus(Long gameId, PromotionStatus status);
    Optional<Promotion> findTopByGameIdAndStatusOrderByIdDesc(
            Long gameId,
            PromotionStatus status
    );
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
            "FROM Promotion p WHERE p.game.id = :gameId " +
            "AND p.status = 'ACTIVE' " +
            "AND p.endDate >= :now")
    boolean existsActiveByGameId(@Param("gameId") Long gameId,
                                 @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.game.id = :gameId " +
            "AND p.status = 'ACTIVE' " +
            "AND p.startDate <= :now " +
            "AND p.endDate >= :now")
    Optional<Promotion> findActiveByGameId(@Param("gameId") Long gameId,
                                           @Param("now") LocalDateTime now);

}
