package com.backend.gamesales.Dto.Response;

import com.backend.gamesales.Model.Enums.CategoryGame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    private Long id;
    private String title;
    private Double price;
    private String description;
    private String developer;
    private String publisher;
    private LocalDate releaseDate;
    private CategoryGame category;
    private String imageUrl;
    private Set<TagResponse> tags;
    private Long sellerId;
    private String sellerName;
    private Double originalPrice;
    private Double finalPrice;
    private Double discountAmount;
    private Double discountPercentage;
    private Boolean hasDiscount;
    private String discountType;
}
