package com.backend.gamesales.Dto.Response;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public class PaymentItemResponse{
    private Long gameId;
    private String gameTitle;
    private Long sellerId;
    private BigDecimal priceAtPurchase;
}
