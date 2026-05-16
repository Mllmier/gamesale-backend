package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Response.EarningItemResponse;
import com.backend.gamesales.Dto.Response.EarningResponse;
import com.backend.gamesales.Exceptions.NotFoundException;
import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Model.PaymentItem;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.PaymentItemRepository;
import com.backend.gamesales.Repository.PaymentRepository;
import com.backend.gamesales.Repository.SellerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerEarningService {
    private final PaymentItemRepository paymentItemRepository;
    private final SellerRepository      sellerRepository;
    private final CommissionService     commissionService;

    private static final String CURRENCY = "COP";


    public EarningResponse getEarnings(Users user) {
        Seller seller = sellerRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Seller profile not found"));

        List<PaymentItem> soldItems = paymentItemRepository
                .findBySellerAndPayment_StatusOrderByPayment_PaidAtDesc(
                        seller.getUser(),
                        PaymentStatus.SUCCEEDED
                );

        List<EarningItemResponse> sales = soldItems.stream()
                .map(this::toEarningItem)
                .toList();

        BigDecimal totalEarnings   = calculateTotalEarnings(soldItems);
        BigDecimal totalCommission = calculateTotalCommission(soldItems);

        log.info("Earnings fetched | sellerId={} | total={} | sales={}",
                seller.getId(), totalEarnings, sales.size());

        return EarningResponse.builder()
                .totalEarnings(totalEarnings)
                .totalCommissionPaid(totalCommission)
                .totalSales(sales.size())
                .currency(CURRENCY)
                .sales(sales)
                .build();
    }

    private BigDecimal calculateTotalEarnings(List<PaymentItem> items) {
        return items.stream()
                .map(item -> commissionService.calculate(item.getPriceAtPurchase()).sellerEarnings())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTotalCommission(List<PaymentItem> items) {
        return items.stream()
                .map(item -> commissionService.calculate(item.getPriceAtPurchase()).commission())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private EarningItemResponse toEarningItem(PaymentItem item) {
        CommissionService.CommissionBreakdown breakdown =
                commissionService.calculate(item.getPriceAtPurchase());

        return EarningItemResponse.builder()
                .paymentId(item.getPayment().getId())
                .gameId(item.getGame().getId())
                .gameTitle(item.getGame().getTitle())
                .priceAtPurchase(item.getPriceAtPurchase())
                .sellerEarnings(breakdown.sellerEarnings())
                .commission(breakdown.commission())
                .paidAt(item.getPayment().getPaidAt())
                .build();
    }
}
