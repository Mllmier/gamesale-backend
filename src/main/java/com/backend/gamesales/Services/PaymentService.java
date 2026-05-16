package com.backend.gamesales.Services;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.gamesales.Dto.PricingResult;
import com.backend.gamesales.Dto.Response.PaymentItemResponse;
import com.backend.gamesales.Dto.Response.PaymentResponse;
import com.backend.gamesales.Exceptions.PaymentException;
import com.backend.gamesales.Infrastructure.StripeCheckout;
import com.backend.gamesales.Model.Cart;
import com.backend.gamesales.Model.CartItem;
import com.backend.gamesales.Model.Enums.CartStatus;
import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Payment;
import com.backend.gamesales.Model.PaymentItem;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.CartRepository;
import com.backend.gamesales.Repository.PaymentRepository;
import com.backend.gamesales.Repository.UserGameRepository;
import com.stripe.model.checkout.Session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository  paymentRepository;
    private final CartRepository     cartRepository;
    private final UserGameRepository userGameRepository;
    private final StripeCheckout stripeCheckout;
    private final PricingService     pricingService;

    @Transactional
    public PaymentResponse initiateCartPayment(Users buyer) {
        Cart cart = cartRepository.findByUserAndStatus(buyer, CartStatus.ACTIVE)
                .orElseThrow(() -> new PaymentException(
                        "No active cart found for user: " + buyer.getId()));

        List<CartItem> items = cart.getItems();

        if (items.isEmpty()) {
            throw new PaymentException("Cannot process payment: cart is empty");
        }
        validateCartItems(items, buyer);

        List<PricingResult> pricingResults = items.stream()
                .map(item -> pricingService.calculate(item.getGame()))
                .toList();

        BigDecimal total = calculateTotal(pricingResults);

        Payment payment = buildPayment(cart, buyer, total);
        payment.setItems(buildPaymentItems(items, pricingResults, payment));
        paymentRepository.save(payment);

        return processStripeSession(payment);
    }

    private void validateCartItems(List<CartItem> items, Users buyer) {


        for (CartItem item : items) {
            Game game = item.getGame();
            validateGameAvailable(game);
            validateNotOwnGame(game, buyer);
            validateNotAlreadyInLibrary(game, buyer);

            if (game.getSeller().getStripeAccountId() == null || game.getSeller().getStripeAccountId().isEmpty()) {
                throw new PaymentException("El vendedor de " + game.getTitle() + " no puede recibir pagos actualmente.");
            }
        }
    }

    private void validateGameAvailable(Game game) {
        if (!game.isActive()) {
            throw new PaymentException("Game is not available: " + game.getTitle());
        }
    }

    private void validateNotOwnGame(Game game, Users buyer) {
        if (game.getSeller().getUser().getId().equals(buyer.getId())) {
            throw new PaymentException("You cannot purchase your own game: " + game.getTitle());
        }
    }

    private void validateNotAlreadyInLibrary(Game game, Users buyer) {
        if (userGameRepository.existsByUserAndGame(buyer, game)) {
            throw new PaymentException("You already own this game: " + game.getTitle());
        }
    }

    private BigDecimal calculateTotal(List<PricingResult> pricingResults) {
        return pricingResults.stream()
                .map(PricingResult::finalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Payment buildPayment(Cart cart, Users buyer, BigDecimal total) {
        return Payment.builder()
                .cart(cart)
                .buyer(buyer)
                .total(total)
                .status(PaymentStatus.PENDING)
                .build();
    }

    private List<PaymentItem> buildPaymentItems(List<CartItem> items,
                                                List<PricingResult> pricingResults,
                                                Payment payment) {
        return IntStream.range(0, items.size())
                .mapToObj(i -> buildPaymentItem(items.get(i), pricingResults.get(i), payment))
                .toList();
    }

    private PaymentItem buildPaymentItem(CartItem item, PricingResult pricing, Payment payment) {
        Game game = item.getGame();

        return PaymentItem.builder()
                .payment(payment)
                .game(game)
                .seller(game.getSeller().getUser())
                .priceAtPurchase(pricing.finalPrice())
                .commissionAmount(pricing.commission())
                .sellerEarnings(pricing.sellerEarnings())
                .discountType(pricing.discountType())
                .build();
    }

    private PaymentResponse processStripeSession(Payment payment) {
        try {
            Session session = stripeCheckout.createCheckoutSession(payment);

            payment.setStripeSessionId(session.getId());
            payment.setStripeCheckoutUrl(session.getUrl());
            paymentRepository.save(payment);

            return toResponse(payment);

        } catch (Exception e) {
            payment.markAsFailed();
            paymentRepository.save(payment);
            log.error("Stripe session failed | paymentId={} | error_class={} | error_message={}",
                    payment.getId(), e.getClass().getName(), e.getMessage(), e);
            
            String errorMessage = "Payment could not be processed. Please try again.";
            if (e instanceof com.stripe.exception.StripeException) {
                errorMessage = "Stripe Error: " + e.getMessage();
            }
            
            throw new PaymentException(errorMessage, e);
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .checkoutUrl(payment.getStripeCheckoutUrl())
                .stripeSessionId(payment.getStripeSessionId())
                .status(payment.getStatus())
                .total(payment.getTotal())
                .items(payment.getItems().stream().map(this::toItemResponse).toList())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private PaymentItemResponse toItemResponse(PaymentItem item) {
        return PaymentItemResponse.builder()
                .gameId(item.getGame().getId())
                .gameTitle(item.getGame().getTitle())
                .sellerId(item.getSeller().getId())
                .priceAtPurchase(item.getPriceAtPurchase())
                .build();
    }
}