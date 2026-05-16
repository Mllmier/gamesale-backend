package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Response.GameResponse;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;


    @GetMapping
    public ResponseEntity<List<GameResponse>> getWishlist(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(wishlistService.getWishlist(user));
    }


    @PostMapping("/{gameId}")
    public ResponseEntity<Map<String, String>> add(
            @PathVariable Long gameId,
            Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        wishlistService.addToWishlist(gameId, user);
        return ResponseEntity.ok(Map.of("message", "Juego agregado a tu wishlist"));
    }


    @DeleteMapping("/{gameId}")
    public ResponseEntity<Map<String, String>> remove(
            @PathVariable Long gameId,
            Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        wishlistService.removeFromWishlist(gameId, user);
        return ResponseEntity.ok(Map.of("message", "Juego eliminado de tu wishlist"));
    }
}
