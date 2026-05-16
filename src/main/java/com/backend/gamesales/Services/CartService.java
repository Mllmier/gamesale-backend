package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Response.CartResponse;
import com.backend.gamesales.Dto.Response.CartItemResponse;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Exceptions.PaymentException;
import com.backend.gamesales.Model.Cart;
import com.backend.gamesales.Model.CartItem;
import com.backend.gamesales.Model.Enums.CartStatus;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartRepository     cartRepository;
    private final CartItemRepository cartItemRepository;
    private final GameRepository     gameRepository;
    private final UsersRepository    usersRepository;
    private final UserGameRepository userGameRepository;

    public CartResponse getCart(String username) {
        Users user = findUserByEmail(username);
        Cart cart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
                .orElseGet(() -> createCart(user));
        return toCartResponse(cart);
    }

    public CartItemResponse  addGameToCart(String username, Long gameId) {
        Users user = findUserByEmail(username);
        Game game  = findGameById(gameId);

        validateGameAvailable(game);
        validateNotOwnGame(game, user);
        validateNotAlreadyInLibrary(game, user);

        Cart cart = cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
                .orElseGet(() -> createCart(user));

        validateNotAlreadyInCart(cart, game);

        CartItem newItem = CartItem.builder()
                .cart(cart)
                .game(game)
                .build();


        CartItem savedItem = cartItemRepository.save(newItem);
        cart.getItems().add(savedItem);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return toCartItemResponse(savedItem);
    }


    public void removeItem(Long itemId, String username) {
        Users user   = findUserByEmail(username);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));

        if (!item.getCart().getUser().getId().equals(user.getId())) {
            throw new PaymentException("Unauthorized: item does not belong to this user");
        }

        cartItemRepository.deleteById(itemId);
    }


    public void clearCart(String username) {
        Users user = findUserByEmail(username);
        cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE)
                .ifPresent(this::deleteCartItems);
    }

    public void clearCartByUserId(Long userId) {
        usersRepository.findById(userId)
                .flatMap(user -> cartRepository.findByUserAndStatus(user, CartStatus.ACTIVE))
                .ifPresent(this::deleteCartItems);
    }

    private void validateGameAvailable(Game game) {
        if (!game.isActive()) {
            throw new PaymentException("Game is not available: " + game.getTitle());
        }
    }

    private void validateNotOwnGame(Game game, Users user) {
        if (game.getSeller().getUser().getId().equals(user.getId())) {
            throw new PaymentException("You cannot add your own game to the cart: " + game.getTitle());
        }
    }

    private void validateNotAlreadyInLibrary(Game game, Users user) {
        if (userGameRepository.existsByUserAndGame(user, game)) {
            throw new PaymentException("You already own this game: " + game.getTitle());
        }
    }

    private void validateNotAlreadyInCart(Cart cart, Game game) {
        if (cartItemRepository.existsByCartAndGame(cart, game)) {
            throw new PaymentException("Game is already in your cart: " + game.getTitle());
        }
    }

    private Cart createCart(Users user) {
        Cart cart = new Cart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    private void deleteCartItems(Cart cart) {
        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    private Users findUserByEmail(String email) {
        return usersRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));
    }

    private Game findGameById(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("Game not found: " + gameId));
    }


    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .items(items)
                .total(total)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .itemId(item.getId())
                .gameId(item.getGame().getId())
                .gameTitle(item.getGame().getTitle())
                .price(item.getGame().getPrice())
                .build();
    }
}
