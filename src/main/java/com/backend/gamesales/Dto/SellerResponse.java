package com.backend.gamesales.Dto;

import com.backend.gamesales.Model.Enums.StatusSeller;
import com.backend.gamesales.Model.Enums.TypeSeller;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
public class SellerResponse {
    private ProfileResponse profile;
    private String description;
    private TypeSeller typeSeller;
    private String companyName;
    private String companyId;
    private StatusSeller status;
    private Double rating;
    private Integer totalSales;
    private Integer totalReviews;
    private LocalDateTime createdAt;

}
