package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserId(Long userId);
    boolean existsByUserIdAndGameId(Long userId, Long gameId);
    void deleteByUserIdAndGameId(Long userId, Long gameId);
    List<Wishlist> findByGameId(Long gameId);
}
