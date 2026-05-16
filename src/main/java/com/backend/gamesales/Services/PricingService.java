package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.PricingResult;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Promotion;
import com.backend.gamesales.Repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {
    private final PromotionRepository promotionRepository;
    private final CommissionService   commissionService;

    public PricingResult calculate(Game game) {

        BigDecimal originalPrice = game.getPrice();

        Optional<Promotion> activePromotion = promotionRepository
                .findActiveByGameId(game.getId(), LocalDateTime.now());

        if (activePromotion.isEmpty()) {
            return buildResult(originalPrice, originalPrice, BigDecimal.ZERO, "NONE");
        }

        Promotion promotion = activePromotion.get();
        String discountType = resolveDiscountType(promotion);
        BigDecimal discountAmount = calculateDiscount(originalPrice, promotion.getDiscountPercentage());
        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        log.info("Promotion applied | game: {} | type: {} | discount: {}% | original: {} | final: {}",
                game.getTitle(), discountType, promotion.getDiscountPercentage(),
                originalPrice, finalPrice);

        return buildResult(originalPrice, finalPrice, discountAmount, discountType);
    }

    private PricingResult buildResult(BigDecimal originalPrice,
                                      BigDecimal finalPrice,
                                      BigDecimal discountAmount,
                                      String discountType) {


        BigDecimal baseForCommission = discountType.equals("ADMIN")
                ? originalPrice
                : finalPrice;

        CommissionService.CommissionBreakdown breakdown =
                commissionService.calculate(baseForCommission);

        return new PricingResult(
                originalPrice,
                finalPrice,
                discountAmount,
                breakdown.commission(),
                breakdown.sellerEarnings(),
                discountType
        );
    }

    private BigDecimal calculateDiscount(BigDecimal price, Double percentage) {
        return price
                .multiply(BigDecimal.valueOf(percentage / 100))
                .setScale(2, RoundingMode.HALF_UP);
    }
    private String resolveDiscountType(Promotion promotion) {
        return promotion.getSeller() == null ? "ADMIN" : "SELLER";
    }
}
