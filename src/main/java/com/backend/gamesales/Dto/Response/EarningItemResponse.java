package com.backend.gamesales.Dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class EarningItemResponse{
    private Long paymentId;
    private Long gameId;
    private String gameTitle;
    private BigDecimal priceAtPurchase;
    private BigDecimal sellerEarnings;
    private BigDecimal commission;
    private LocalDateTime paidAt;
}
