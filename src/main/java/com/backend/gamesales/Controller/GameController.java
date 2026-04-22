package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.GameRequest;
import com.backend.gamesales.Exceptions.ForbiddenException;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Model.Enums.CategoryGame;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.SellerRepository;
import com.backend.gamesales.Services.GameService;
import com.backend.gamesales.Services.StorageService;
import com.backend.gamesales.Utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.backend.gamesales.Utils.ErrorResponseBuilder.buildErrorResponse;

@RestController
@RequestMapping("api/games")
public class GameController {

    @Autowired
    private GameService  gameService;
    @Autowired private
    SellerRepository sellerRepository;
    @Autowired private
    StorageService  supabaseStorageService;

    private Seller getSellerFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthorized: token missing or invalid");
        }
        Users user = (Users) authentication.getPrincipal();
        return sellerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @ModelAttribute GameRequest request,
            Authentication authentication) {
        try {
            Seller seller = getSellerFromAuth(authentication);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(gameService.uploadGame(request, seller));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }
    @GetMapping
    public ResponseEntity<?> listGame(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pagination pagination = Pagination.of(page, size);

        return ResponseEntity.ok(
                gameService.getAll(pagination)
        );
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(gameService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(buildErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND));
        }
    }
    @GetMapping("/search")
    public List<Game> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) CategoryGame categoryGame,
            @RequestParam(required = false) Double price) {
        return gameService.search(title, categoryGame, price);
    }
    @GetMapping("/tag/{tagId}")
    public List<Game> getByTag(@PathVariable Long tagId) {
        return gameService.getByTag(tagId);
    }
    @GetMapping("/seller/mine")
    public ResponseEntity<?> getMyGames(Authentication authentication) {
        try {
            Seller seller = getSellerFromAuth(authentication);
            return ResponseEntity.ok(gameService.getMyGames(seller.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(buildErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam Double price,
            @RequestParam String description,
            @RequestParam String developer,
            @RequestParam String publisher,
            @RequestParam String releaseDate,
            @RequestParam String category,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) Set<Long> tags,
            Authentication authentication) {

        try {
            Seller seller = getSellerFromAuth(authentication);

            Game updatedData = new Game();
            updatedData.setTitle(title);
            updatedData.setPrice(price);
            updatedData.setDescription(description);
            updatedData.setDeveloper(developer);
            updatedData.setPublisher(publisher);
            updatedData.setReleaseDate(LocalDate.parse(releaseDate));
            updatedData.setCategoryGame(CategoryGame.valueOf(category.toUpperCase()));

            return ResponseEntity.ok(
                    gameService.update(id, updatedData, image, tags, seller.getId())
            );

        } catch (RuntimeException e) {
            HttpStatus status = e.getMessage().contains("Forbidden")
                    ? HttpStatus.FORBIDDEN : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status)
                    .body(buildErrorResponse(e.getMessage(), status));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            Seller seller = getSellerFromAuth(authentication);
            gameService.delete(id, seller.getId());
            return ResponseEntity.noContent().build();

        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(buildErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN));
            }
        }

}