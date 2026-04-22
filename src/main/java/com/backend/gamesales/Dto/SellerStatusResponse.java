package com.backend.gamesales.Dto;

import com.backend.gamesales.Model.Enums.StatusSeller;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class SellerStatusResponse {
    private StatusSeller status;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private String message;
}
