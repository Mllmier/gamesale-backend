package com.backend.gamesales.Repository;
import com.backend.gamesales.Model.Cart;
import com.backend.gamesales.Model.CartItem;
import com.backend.gamesales.Model.Game;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem , Long> {
    Optional<CartItem> findByCartAndGame(Cart cart, Game game);
    boolean existsByCartAndGame(Cart cart, Game game);

}

