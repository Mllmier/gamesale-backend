package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.UserGame;
import com.backend.gamesales.Model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    boolean existsByUserIdAndGameId(Long userId, Long gameId);
    Page<UserGame> findByUserIdOrderByPurchasedAtDesc(Long userId, Pageable pageable);
    boolean existsByUserAndGame(Users user, Game game);
    List<UserGame> findByUserOrderByPurchasedAtDesc(Users user);

}
