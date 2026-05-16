package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Response.GameResponse;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Model.Wishlist;
import com.backend.gamesales.Repository.GameRepository;
import com.backend.gamesales.Repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;

    public void addToWishlist(Long gameId, Users user) {
        if (wishlistRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new RuntimeException("The game is already on your wishlist");
        }
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameId));

        Wishlist entry = new Wishlist();
        entry.setUser(user);
        entry.setGame(game);
        wishlistRepository.save(entry);
    }

    @Transactional
    public void removeFromWishlist(Long gameId, Users user) {
        if (!wishlistRepository.existsByUserIdAndGameId(user.getId(), gameId)) {
            throw new NotFoundException("The game is not on your wishlist");
        }
        wishlistRepository.deleteByUserIdAndGameId(user.getId(), gameId);
    }

    public List<GameResponse> getWishlist(Users user) {
        return wishlistRepository.findByUserId(user.getId()).stream()
                .map(w -> gameService.getGameResponseById(w.getGame().getId()))
                .toList();
    }
}
