package com.backend.gamesales.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PromotionRequest {
    @NotBlank(message = "Title is required")
    private String title;
    private String description;
    @NotNull(message = "Discount is required")
    private Double discountPercentage;
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    private Long gameId;

}
