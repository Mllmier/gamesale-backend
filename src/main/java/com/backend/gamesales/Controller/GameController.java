package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Response.GameResponse;
import com.backend.gamesales.Dto.Response.SellerStatusResponse;
import com.backend.gamesales.Dto.Request.GameRequest;
import com.backend.gamesales.Model.Enums.CategoryGame;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Tags;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Services.GameService;
import com.backend.gamesales.Services.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final SellerRepository sellerRepository;
    private final TagService tagService;

    private Seller getSellerFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No autorizado: token faltante o inválido");
        }
        Users user = (Users) authentication.getPrincipal();
        return sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
    }

    @PostMapping(value = "/upload", consumes = { "multipart/form-data" })
    public ResponseEntity<GameResponse> upload(
            @ModelAttribute @Valid GameRequest request,
            Authentication authentication) {

        Seller seller = getSellerFromAuth(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameService.uploadGame(request, seller));
    }

    // PATCH /api/games/{id}/toggle
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<GameResponse> toggleActive(
            @PathVariable Long id,
            Authentication authentication) {
        Seller seller = getSellerFromAuth(authentication);
        return ResponseEntity.ok(gameService.toggleActive(id, seller.getId()));
    }

    @GetMapping
    public ResponseEntity<Page<GameResponse>> listGame(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(gameService.getAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameResponseById(id));
    }

    @GetMapping("/seller/stats")
    public ResponseEntity<SellerStatusResponse> getMyStats(Authentication authentication) {
        Seller seller = getSellerFromAuth(authentication);
        return ResponseEntity.ok(gameService.getSellerStats(seller.getId()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<GameResponse>> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) CategoryGame category,
            @RequestParam(required = false) Double price) {
        return ResponseEntity.ok(gameService.search(title, category, price));
    }

    @GetMapping("/tag/{tagId}")
    public ResponseEntity<List<GameResponse>> getByTag(@PathVariable Long tagId) {
        return ResponseEntity.ok(gameService.getByTag(tagId));
    }

    @GetMapping("/my-games")
    public ResponseEntity<List<GameResponse>> getMyGames(Authentication authentication) {
        Seller seller = getSellerFromAuth(authentication);
        return ResponseEntity.ok(gameService.getMyGames(seller.getId()));
    }

    @PutMapping(value = "/{id}", consumes = { "multipart/form-data" })
    public ResponseEntity<GameResponse> update(
            @PathVariable Long id,
            @ModelAttribute @Valid GameRequest request,
            Authentication authentication) {
        Seller seller = getSellerFromAuth(authentication);
        return ResponseEntity.ok(gameService.update(id, request, seller.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            Authentication authentication) {
        Seller seller = getSellerFromAuth(authentication);
        gameService.delete(id, seller.getId());
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/tags/all")
    public ResponseEntity<List<Tags>> getAllTags() {
        return ResponseEntity.ok(tagService.getAll());
    }
}
