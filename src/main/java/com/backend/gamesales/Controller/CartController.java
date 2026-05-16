package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Request.CartRequest;
import com.backend.gamesales.Dto.Response.CartItemResponse;
import com.backend.gamesales.Dto.Response.CartResponse;
import com.backend.gamesales.Model.Cart;
import com.backend.gamesales.Model.CartItem;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.CartService;
import com.backend.gamesales.Services.UserDetailsServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // CartController.java
    @PostMapping("/items")
    public ResponseEntity<CartItemResponse> addItem(
            @RequestBody CartRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        CartItemResponse item = cartService.addGameToCart(userDetails.getUsername(), request.getGameId());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails) {

        cartService.removeItem(itemId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
