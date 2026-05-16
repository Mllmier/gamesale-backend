package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Request.PromotionRequest;
import com.backend.gamesales.Dto.Response.PromotionResponse;
import com.backend.gamesales.Events.PromotionActivatedEvent;
import com.backend.gamesales.Model.Enums.DiscountType;
import com.backend.gamesales.Model.Enums.PromotionStatus;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Promotion;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.GameRepository;
import com.backend.gamesales.Repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRepository promotionRepository;
    private final GameRepository gameRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PromotionResponse createPromotion(PromotionRequest request, Users seller) {

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        if (promotionRepository.existsByGameIdAndStatus(game.getId(), PromotionStatus.ACTIVE)) {
            throw new RuntimeException("This game already has an active promotion");
        }


        validateDiscount(request.getDiscountPercentage());
        validateDates(request.getStartDate(), request.getEndDate());

        Promotion promotion = Promotion.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .discountPercentage(request.getDiscountPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .game(game)
                .seller(seller)
                .discountType(DiscountType.SELLER)
                .status(PromotionStatus.ACTIVE)
                .build();
        Promotion saved = promotionRepository.save(promotion);
        eventPublisher.publishEvent(new PromotionActivatedEvent(this, saved, game));

        return toResponse(saved);
    }
    public List<PromotionResponse> getActivePromotions() {
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository
                .findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        PromotionStatus.ACTIVE,
                        now,
                        now
                );
        return promotions.stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateDiscount(Double discount) {
        if (discount < 5 || discount > 60) {
            throw new RuntimeException("Discount must be between 5% and 60%");
        }
    }
    public PromotionResponse getPromotionByGame(Long gameId) {
        Promotion promotion = promotionRepository
                .findTopByGameIdAndStatusOrderByIdDesc(
                        gameId,
                        PromotionStatus.ACTIVE
                )
                .orElseThrow(() -> new RuntimeException("No active promotion for this game"));
        return toResponse(promotion);
    }

    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new RuntimeException("Invalid dates");
        }
    }

    private PromotionResponse toResponse(Promotion p) {
        return PromotionResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .discountPercentage(p.getDiscountPercentage())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .build();
    }
}