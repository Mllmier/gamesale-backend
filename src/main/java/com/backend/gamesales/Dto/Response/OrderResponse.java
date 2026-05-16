package com.backend.gamesales.Dto.Response;

import com.backend.gamesales.Model.Enums.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {
    private Long orderId;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private List<PaymentItemResponse> items;
    private OrderStatus status;
}
