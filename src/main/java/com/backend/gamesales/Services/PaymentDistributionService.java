package com.backend.gamesales.Services;

import com.backend.gamesales.Exceptions.PaymentException;
import com.backend.gamesales.Infrastructure.StripeConnect;
import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Model.Payment;
import com.backend.gamesales.Model.PaymentItem;
import com.backend.gamesales.Model.Seller;
import com.backend.gamesales.Repository.PaymentRepository;
import com.backend.gamesales.Repository.SellerRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentDistributionService {

    private final PaymentRepository paymentRepository;
    private final SellerRepository sellerRepository;
    private final StripeConnect stripeConnect;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void distributePayment(Payment payment) {
        log.info("Distributing payment | paymentId={} | total={}",
                payment.getId(), payment.getTotal());

        if (isAlreadyDistributed(payment)) {
            log.info("Payment already distributed, skipping | paymentId={}", payment.getId());
            return;
        }

        validateReadyForDistribution(payment);

        Map<Long, SellerPayout> payouts = aggregateBySeller(payment.getItems());
        logDistributionSummary(payment, payouts);

        List<Long> failedSellers = executeTransfers(payment.getId(), payouts, payment.getStripeChargeId());

        if (failedSellers.isEmpty()) {
            payment.markAsDistributed();
            paymentRepository.save(payment);
            log.info("Distribution completed | paymentId={}", payment.getId());
        } else {
            log.warn("Distribution partially failed | paymentId={} | failedSellers={}",
                    payment.getId(), failedSellers);
        }
    }
    private boolean isAlreadyDistributed(Payment payment) {
        return payment.getStatus() == PaymentStatus.DISTRIBUTED;
    }

    private void validateReadyForDistribution(Payment payment) {
        if (payment.getStatus() != PaymentStatus.SUCCEEDED) {
            throw new PaymentException(
                    "Cannot distribute payment in status: " + payment.getStatus());
        }
        if (payment.getItems() == null || payment.getItems().isEmpty()) {
            throw new PaymentException(
                    "Payment has no items | paymentId=" + payment.getId());
        }
    }

    private Map<Long, SellerPayout> aggregateBySeller(List<PaymentItem> items) {
        Map<Long, SellerPayout> result = new HashMap<>();

        for (PaymentItem item : items) {
            Long sellerId = item.getSeller().getId();

            SellerPayout payout = new SellerPayout(
                    sellerId,
                    item.getSellerEarnings(),
                    item.getCommissionAmount(),
                    1
            );

            result.merge(sellerId, payout, SellerPayout::merge);
        }

        return result;
    }

    private void logDistributionSummary(Payment payment, Map<Long, SellerPayout> payouts) {
        BigDecimal totalToSellers = payouts.values().stream()
                .map(SellerPayout::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal adminCommission = payment.getTotal().subtract(totalToSellers);

        log.info("Summary | paymentId={} | sellers={} | toSellers={} | adminFee={}",
                payment.getId(), payouts.size(), totalToSellers, adminCommission);
    }


    private List<Long> executeTransfers(Long paymentId, Map<Long, SellerPayout> payouts, String chargeId) {
        List<Long> failedSellers = new ArrayList<>();

        for (SellerPayout payout : payouts.values()) {
            try {
                executeTransfer(paymentId, payout, chargeId);
                log.info("Transfer succeeded | paymentId={} | sellerId={} | amount={} | games={}",
                        paymentId, payout.sellerId(), payout.amount(), payout.gameCount());
            } catch (Exception e) {
                log.error("Transfer failed | paymentId={} | sellerId={} | amount={} | error={}",
                        paymentId, payout.sellerId(), payout.amount(), e.getMessage());
                recordFailedTransfer(paymentId, payout);
                failedSellers.add(payout.sellerId());
            }
        }

        return failedSellers;
    }


    private void executeTransfer(Long paymentId, SellerPayout payout,String chargeId) {
        Seller seller = sellerRepository.findByUserId(payout.sellerId())
                .orElseThrow(() -> new PaymentException(
                        "Seller not found for userId: " + payout.sellerId()));

        if (seller.getStripeAccountId() == null || seller.getStripeAccountId().isBlank()) {
            throw new PaymentException(
                    "Seller has no Stripe account | sellerId=" + seller.getId());
        }

        String reference = paymentId + "_" + payout.sellerId();

        stripeConnect.transferToSeller(
                seller.getStripeAccountId(),
                payout.amount(),
                reference,
                chargeId
        );
    }


    private void recordFailedTransfer(Long paymentId, SellerPayout payout) {
        log.warn("PENDING_RETRY | paymentId={} | sellerId={} | amount={}",
                paymentId, payout.sellerId(), payout.amount());
    }

    private record SellerPayout(
            Long sellerId,
            BigDecimal amount,
            BigDecimal commission,
            int gameCount
    ) {
        SellerPayout merge(SellerPayout other) {
            return new SellerPayout(
                    this.sellerId,
                    this.amount.add(other.amount),
                    this.commission.add(other.commission),
                    this.gameCount + other.gameCount
            );
        }
    }
}
