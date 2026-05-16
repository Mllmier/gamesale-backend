package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.Cart;
import com.backend.gamesales.Model.CartItem;
import com.backend.gamesales.Model.Enums.CartStatus;
import com.backend.gamesales.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {

    Optional<Cart> findByUserAndStatus(Users user, CartStatus status);

    Optional<Cart> findByUser(Users user);

    @Query("SELECT c FROM Cart c WHERE c.status = :status AND c.updatedAt < :threshold")
    List<Cart> findAbandonedCarts(@Param("status") CartStatus status,
                                  @Param("threshold") LocalDateTime threshold);
}
