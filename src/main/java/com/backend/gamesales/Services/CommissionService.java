package com.backend.gamesales.Services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class CommissionService {

    @Value("${platform.commission.rate:0.18}")
    private BigDecimal commissionRate;

    public CommissionBreakdown calculate(BigDecimal amount) {
        BigDecimal commission = amount.multiply(commissionRate);
        BigDecimal sellerEarnings = amount.subtract(commission);
        return new CommissionBreakdown(commission, sellerEarnings);
    }

    public record CommissionBreakdown(
            BigDecimal commission,
            BigDecimal sellerEarnings
    ) {}
}
