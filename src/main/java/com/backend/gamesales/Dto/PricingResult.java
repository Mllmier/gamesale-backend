package com.backend.gamesales.Dto;

import java.math.BigDecimal;

public record PricingResult(BigDecimal originalPrice,
                            BigDecimal finalPrice,
                            BigDecimal discountAmount,
                            BigDecimal commission,
                            BigDecimal sellerEarnings,
                            String discountType) {
}
