package com.backend.gamesales.Dto.Response;

import com.backend.gamesales.Model.Enums.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder
public class PaymentResponse{
    private Long paymentId;
    private String checkoutUrl;
    private String gameTitle;

    private String stripeSessionId;
    private PaymentStatus status;
    private BigDecimal total;
    private List<PaymentItemResponse> items;
    private LocalDateTime createdAt;

}
