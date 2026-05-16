package com.backend.gamesales.Dto.Request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
    private Long userId;
    @NotNull(message = "Game ID is required")
    private Long gameId;
}
