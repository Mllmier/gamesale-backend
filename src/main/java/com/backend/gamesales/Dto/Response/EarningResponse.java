package com.backend.gamesales.Dto.Response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class EarningResponse {
    private BigDecimal totalEarnings;
    private String gameTitle;
    private BigDecimal totalCommissionPaid;
    private Integer totalSales;
    private String currency;
    private List<EarningItemResponse> sales;
}
