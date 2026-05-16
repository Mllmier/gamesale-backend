package com.backend.gamesales.Dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserGameResponse {
    private Long id;
    private LocalDateTime purchasedAt;
    private Long gameId;
    private String gameTitle;
    private String gameImageUrl;
    private String developer;
    private BigDecimal price;
}
