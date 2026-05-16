package com.backend.gamesales.Dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class CartResponse {
    private Long cartId;
    private BigDecimal total;
    private List<CartItemResponse> items;
}
