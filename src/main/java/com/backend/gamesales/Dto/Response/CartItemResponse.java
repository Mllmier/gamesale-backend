package com.backend.gamesales.Dto.Response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long itemId;
    private Long gameId;
    private String gameTitle;
    private BigDecimal price;

}
