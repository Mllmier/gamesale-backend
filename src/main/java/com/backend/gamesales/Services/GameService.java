package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.PricingResult;
import com.backend.gamesales.Dto.Request.GameRequest;
import com.backend.gamesales.Dto.Response.GameResponse;
import com.backend.gamesales.Dto.Response.SellerStatusResponse;
import com.backend.gamesales.Dto.Response.TagResponse;
import com.backend.gamesales.Infrastructure.Storage.Storage;
import com.backend.gamesales.Model.Enums.CategoryGame;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Repository.GameRepository;
import com.backend.gamesales.Repository.PromotionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final Storage storage;
    private final TagService tagService;
    private final PricingService pricingService;
    private final PromotionRepository promotionRepository;

    @Transactional
    public GameResponse uploadGame(GameRequest request, Seller seller) {
        if (gameRepository.existsByTitleIgnoreCaseAndSellerId(
                request.getTitle().trim(), seller.getId())) {
            throw new RuntimeException("You already have a game registered with this title.");
        }

        if (request.getImage() == null || request.getImage().isEmpty()) {
            throw new RuntimeException("The game image is required");
        }

        Game game = Game.builder()
                .title(request.getTitle())
                .price(request.getPrice())
                .description(request.getDescription())
                .developer(request.getDeveloper())
                .publisher(request.getPublisher())
                .releaseDate(LocalDate.parse(request.getReleaseDate()))
                .categoryGame(CategoryGame.valueOf(request.getCategory().toUpperCase()))
                .seller(seller)
                .tags(tagService.resolveTags(request.getTagIds()))
                .active(true)
                .build();

        game = gameRepository.save(game);

        String imageUrl = storage.saveImage(request.getImage(), game.getId());
        game.setImageUrl(imageUrl);

        return mapToResponse(gameRepository.save(game));
    }
    @Transactional
    public GameResponse toggleActive(Long id, Long sellerId) {
        Game game = getById(id);
        checkOwnership(game, sellerId);
        game.setActive(!game.isActive());
        return mapToResponse(gameRepository.save(game));
    }

    public SellerStatusResponse getSellerStats(Long sellerId) {
        List<Game> games = gameRepository.findBySellerId(sellerId);
        return SellerStatusResponse.builder()
                .totalGames(games.size())
                .activeGames((int) games.stream().filter(Game::isActive).count())
                .inactiveGames((int) games.stream().filter(g -> !g.isActive()).count())
                .build();
    }

    @Transactional
    public GameResponse update(Long id, GameRequest request, Long sellerId) {
        Game game = getById(id);
        checkOwnership(game, sellerId);

        game.setTitle(request.getTitle());
        game.setPrice(request.getPrice());
        game.setDescription(request.getDescription());
        game.setDeveloper(request.getDeveloper());
        game.setPublisher(request.getPublisher());
        game.setReleaseDate(LocalDate.parse(request.getReleaseDate()));
        game.setCategoryGame(CategoryGame.valueOf(request.getCategory().toUpperCase()));

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String newImageUrl = storage.replaceImage(
                    game.getImageUrl(),
                    request.getImage(),
                    id);
            game.setImageUrl(newImageUrl);
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            game.setTags(tagService.resolveTags(request.getTagIds()));
        }

        return mapToResponse(gameRepository.save(game));
    }

    public void delete(Long id, Long sellerId) {
        Game game = getById(id);
        checkOwnership(game, sellerId);
        promotionRepository.deleteByGameId(id);
        game.getTags().clear();
        gameRepository.save(game);
        if (game.getImageUrl() != null) {
            storage.deleteImage(game.getImageUrl());
        }

        gameRepository.delete(game);
    }
    public Game getById(Long id) {
        return gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found : " + id));
    }

    public GameResponse getGameResponseById(Long id) {
        return mapToResponse(getById(id));
    }

    public List<GameResponse> getMyGames(Long sellerId) {
        return gameRepository.findBySellerId(sellerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<GameResponse> search(String title, CategoryGame categoryGame, Double price) {
        List<Game> games;
        if (title != null) {
            games = gameRepository.findByTitleContainingIgnoreCase(title);
        } else if (categoryGame != null) {
            games = gameRepository.findByCategoryGame(categoryGame);
        } else if (price != null) {
            games = gameRepository.findByPriceLessThanEqual(price);
        } else {
            games = gameRepository.findAll();
        }
        return games.stream().map(this::mapToResponse).toList();
    }

    public List<GameResponse> getByTag(Long tagId) {
        return gameRepository.findByTagsId(tagId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Page<GameResponse> getAll(Pageable pageable) {
        return gameRepository.findAll(pageable).map(this::mapToResponse);
    }

    private void checkOwnership(Game game, Long sellerId) {
        if (!Objects.equals(game.getSeller().getId(), sellerId)) {
            throw new RuntimeException("You are not the owner of this game");
        }
    }


    private GameResponse mapToResponse(Game game) {
        PricingResult pricing = pricingService.calculate(game);
        boolean hasDiscount = pricing.discountAmount().compareTo(BigDecimal.ZERO) > 0;

        return GameResponse.builder()
                .id(game.getId())
                .title(game.getTitle())
                .originalPrice(pricing.originalPrice().doubleValue())
                .finalPrice(pricing.finalPrice().doubleValue())
                .discountAmount(hasDiscount ? pricing.discountAmount().doubleValue() : null)
                .discountPercentage(hasDiscount ? resolveDiscountPercentage(pricing) : null)
                .hasDiscount(hasDiscount)
                .discountType(hasDiscount ? pricing.discountType() : null)
                .description(game.getDescription())
                .developer(game.getDeveloper())
                .publisher(game.getPublisher())
                .releaseDate(game.getReleaseDate())
                .category(game.getCategoryGame())
                .imageUrl(game.getImageUrl())
                .sellerId(game.getSeller().getId())
                .sellerName(game.getSeller().getStoreName())
                .tags(game.getTags().stream()
                        .map(tag -> new TagResponse(tag.getId(), tag.getName()))
                        .collect(Collectors.toSet()))
                .build();
    }

    private Double resolveDiscountPercentage(PricingResult pricing) {
        if (pricing.originalPrice().compareTo(BigDecimal.ZERO) == 0) return 0.0;
        return pricing.discountAmount()
                .divide(pricing.originalPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

}
