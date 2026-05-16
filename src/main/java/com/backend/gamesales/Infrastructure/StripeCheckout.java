package com.backend.gamesales.Infrastructure;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.gamesales.Config.StripeConfig;
import com.backend.gamesales.Model.Payment;
import com.backend.gamesales.Model.PaymentItem;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StripeCheckout {

    private final StripeConfig stripeConfig;
    private static final String CURRENCY = "COP";

    public Session createCheckoutSession(Payment payment) throws StripeException {
        List<SessionCreateParams.LineItem> lineItems = buildLineItems(payment.getItems());

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeConfig.getSuccessUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(stripeConfig.getCancelUrl())
                .putMetadata("paymentId", String.valueOf(payment.getId()))
                .putMetadata("buyerId",   String.valueOf(payment.getBuyer().getId()))
                .putMetadata("cartId",    String.valueOf(payment.getCart().getId()))
                .addAllLineItem(lineItems)
                .build();


        return Session.create(params);
    }

    private List<SessionCreateParams.LineItem> buildLineItems(List<PaymentItem> items) {
        return items.stream()
                .map(this::toLineItem)
                .toList();
    }

    private SessionCreateParams.LineItem toLineItem(PaymentItem item) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(buildPriceData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData buildPriceData(PaymentItem item) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(CURRENCY)
                .setUnitAmount(toCents(item.getPriceAtPurchase()))
                .setProductData(buildProductData(item))
                .build();
    }

    private SessionCreateParams.LineItem.PriceData.ProductData buildProductData(PaymentItem item) {
        SessionCreateParams.LineItem.PriceData.ProductData.Builder builder =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName(item.getGame().getTitle())
                        .setDescription("Digital game purchase: " + item.getGame().getTitle());

        if (item.getGame().getImageUrl() != null) {
            builder.addImage(item.getGame().getImageUrl());
        }

        return builder.build();
    }

    private long toCents(BigDecimal amount) {
        if (amount == null) return 0L;
        return amount.multiply(BigDecimal.valueOf(100)).setScale(0, java.math.RoundingMode.HALF_UP).longValue();
    }
}