package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.GameRequest;
import com.backend.gamesales.Model.Enums.CategoryGame;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Tags;
import com.backend.gamesales.Repository.GameRepository;
import com.backend.gamesales.Repository.TagRepository;
import com.backend.gamesales.Utils.Pagination;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor

public class GameService {

    @Autowired
    private  GameRepository gameRepository;

    @Autowired
    private final StorageService storageService;

    @Autowired
    private final TagRepository tagRepository;

    @Autowired
    private final TagService tagService;

    @Transactional
    public Game uploadGame(GameRequest request, Seller seller) {

        if (gameRepository.existsByTitleIgnoreCaseAndSellerId(
                request.getTitle().trim(), seller.getId())) {
            throw new RuntimeException("You already have a game registered with this title");
        }
        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new RuntimeException("Game image is required");
        }
        MultipartFile image = request.getImage();
        Game game = new Game();
        game.setTitle(request.getTitle());
        game.setPrice(request.getPrice());
        game.setDescription(request.getDescription());
        game.setDeveloper(request.getDeveloper());
        game.setPublisher(request.getPublisher());
        game.setReleaseDate(LocalDate.parse(request.getReleaseDate()));
        game.setCategoryGame(CategoryGame.valueOf(request.getCategory().toUpperCase()));
        game.setSeller(seller);

        Set<Tags> tags = tagService.resolveTags(request.getTagIds());
        game.setTags(tags);
        game = gameRepository.save(game);

        if (image != null && !image.isEmpty()) {
            String imageUrl = storageService.saveImage(image, game.getId());
            game.setImageUrl(imageUrl);
        }
        return gameRepository.save(game);
    }

    @Transactional
    public Game update(Long id, Game updatedData, MultipartFile image, Set<Long> tags, Long sellerId) {

        Game game = getById(id);

        checkOwnership(game, sellerId);

        game.setTitle(updatedData.getTitle());
        game.setPrice(updatedData.getPrice());
        game.setDescription(updatedData.getDescription());
        game.setDeveloper(updatedData.getDeveloper());
        game.setPublisher(updatedData.getPublisher());
        game.setReleaseDate(updatedData.getReleaseDate());
        game.setCategoryGame(updatedData.getCategoryGame());
        if (image != null && !image.isEmpty()) {
            String newImageUrl = storageService.replaceImage(
                    game.getImageUrl(),
                    image,
                    id
            );
            game.setImageUrl(newImageUrl);
        }
        if (tags != null && !tags.isEmpty()) {
            game.setTags(tagService.resolveTags(tags));
        }

        return gameRepository.save(game);
    }
    public void delete(Long id, Long sellerId) {
        Game game = getById(id);
        checkOwnership(game, sellerId);
        gameRepository.deleteById(id);
    }

    public void deleteGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        storageService.deleteImage(game.getImageUrl());
        gameRepository.delete(game);
    }

    public Game updateImage(Long id, MultipartFile image, Long sellerId) {
        Game game = getById(id);
        checkOwnership(game, sellerId);
        String imageUrl = storageService.saveImage(image, game.getId());
        game.setImageUrl(imageUrl);
        return gameRepository.save(game);
    }
    private void validateGame(Game game) {
        if (game.getPrice() == null || game.getPrice() <= 0)
            throw new RuntimeException("Price must be greater than 0");
        if (game.getTitle() == null || game.getTitle().isBlank())
            throw new RuntimeException("Title is required");
        if (game.getDescription() == null || game.getDescription().isBlank())
            throw new RuntimeException("Description is required");
        if (game.getReleaseDate() == null)
            throw new RuntimeException("Release date is required");
        if (game.getDeveloper() == null || game.getDeveloper().isBlank())
            throw new RuntimeException("Developer is required");
        if (game.getCategoryGame() == null)
            throw new RuntimeException("Category is required");
    }

    private void checkOwnership(Game game, Long sellerId) {
        if (!Objects.equals(game.getSeller().getId(), sellerId)) {
            throw new RuntimeException("Forbidden: you are not the owner of this game");
        }
    }
    public Game getById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found with id: " + id));
    }
    public List<Game> getMyGames(Long sellerId) {
        return gameRepository.findBySellerId(sellerId);
    }

    public List<Game> search(String title, CategoryGame categoryGame, Double price) {

        if (title != null) {
            return gameRepository.findByTitleContainingIgnoreCase(title);
        }
        if (categoryGame != null) {
            return gameRepository.findByCategoryGame(categoryGame);
        }
        if (price != null) {
            return gameRepository.findByPriceLessThanEqual(price);
        }
        return gameRepository.findAll();
    }
    public List<Game> getByTag(Long tagId) {
        return gameRepository.findByTagsId(tagId);
    }


    public Page<Game> getAll(Pagination pagination) {
        Pageable pageable = PageRequest.of(
                pagination.getPage(),
                pagination.getSize()
        );
        return gameRepository.findAll(pageable);
    }
}
