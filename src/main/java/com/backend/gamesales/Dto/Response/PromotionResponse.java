package com.backend.gamesales.Dto.Response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class PromotionResponse {
    private Long id;
    private String title;
    private String description;
    private Double discountPercentage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
