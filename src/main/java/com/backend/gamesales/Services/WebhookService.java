package com.backend.gamesales.Services;

import com.backend.gamesales.Infrastructure.MailBody;
import com.backend.gamesales.Model.*;
import com.backend.gamesales.Model.Enums.OrderStatus;
import com.backend.gamesales.Model.Enums.PaymentStatus;
import com.backend.gamesales.Repository.*;
import com.backend.gamesales.Exceptions.PaymentException;
import com.backend.gamesales.Config.StripeConfig;
import com.backend.gamesales.Infrastructure.EmailSender;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final PaymentRepository          paymentRepository;
    private final UserGameRepository         userGameRepository;
    private final OrderRepository            orderRepository;
    private final NotificationService        notificationService;
    private final EmailSender emailSender;
    private final StripeConfig               stripeConfig;
    private final InvoiceService             invoiceService;
    private final CartService                cartService;
    private final PaymentDistributionService distributionService;

    private static final String EVENT_CHECKOUT_COMPLETED = "checkout.session.completed";


    @Transactional
    public void processWebhook(byte[] payload, String sigHeader) {
        Event event = constructAndValidateEvent(payload, sigHeader);
        log.info("Stripe webhook received | type={} | id={}", event.getType(), event.getId());

        switch (event.getType()) {
            case EVENT_CHECKOUT_COMPLETED -> handleCheckoutCompleted(event);
            default -> log.info("Unhandled Stripe event | type={}", event.getType());
        }
    }


    private void handleCheckoutCompleted(Event event) {
        Session session = deserializeSession(event);
        Long paymentId = parsePaymentId(session.getMetadata().get("paymentId"));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Duplicate webhook ignored | paymentId={}", paymentId);
            return;
        }

        Users buyer = payment.getBuyer();

        payment.markAsPaid();
        payment.setStripeChargeId(resolveChargeId(session));
        paymentRepository.save(payment);

        createOrder(payment, buyer);

        List<Game> deliveredGames = deliverAllGames(payment, buyer);
        String gameTitles = buildGameTitlesList(deliveredGames);

        notifyBuyer(buyer, gameTitles);
        notifySellers(payment.getItems(), deliveredGames);
        sendInvoiceSafely(payment, gameTitles, buyer);
        cartService.clearCartByUserId(buyer.getId());

        distributePaymentSafely(payment, paymentId);

        log.info("Checkout completed | paymentId={} | buyerId={} | gamesDelivered={}",
                paymentId, buyer.getId(), deliveredGames.size());
    }
    private void createOrder(Payment payment, Users buyer) {
        Order order = Order.builder()
                .buyer(buyer)
                .payment(payment)
                .total(payment.getTotal())
                .status(OrderStatus.COMPLETED)
                .build();

        orderRepository.save(order);
        log.info("Order created | paymentId={} | buyerId={}", payment.getId(), buyer.getId());
    }


    private List<Game> deliverAllGames(Payment payment, Users buyer) {
        return payment.getItems().stream()
                .map(item -> deliverSingleGame(item, buyer))
                .filter(Objects::nonNull)
                .toList();
    }

    private Game deliverSingleGame(PaymentItem item, Users buyer) {
        Game game = item.getGame();

        if (userGameRepository.existsByUserAndGame(buyer, game)) {
            log.warn("User already owns game | userId={} | gameId={}", buyer.getId(), game.getId());
            return null;
        }

        userGameRepository.save(UserGame.builder().user(buyer).game(game).build());
        log.info("Game delivered | userId={} | gameId={}", buyer.getId(), game.getId());

        return game;
    }


    private void notifyBuyer(Users buyer, String gameTitles) {
        notificationService.notifyPurchaseSucceeded(buyer.getId(), gameTitles);
        emailSender.sendSimpleMessage(new MailBody(
                buyer.getEmail(),
                "Purchase confirmed",
                buildConfirmationEmailBody(buyer, gameTitles)
        ));
    }

    private void notifySellers(List<PaymentItem> items, List<Game> deliveredGames) {
        items.stream()
                .filter(item -> deliveredGames.contains(item.getGame()))
                .forEach(item -> notificationService.notifyGameSold(
                        item.getSeller().getId(),
                        item.getGame().getTitle()
                ));
    }

    private void sendInvoiceSafely(Payment payment, String gameTitles, Users buyer) {
        try {
            invoiceService.createAndSendInvoice(payment, gameTitles, buyer);
        } catch (Exception e) {
            log.error("Invoice creation failed | paymentId={} | error={}",
                    payment.getId(), e.getMessage());
        }
    }
    private Event constructAndValidateEvent(byte[] payloadBytes, String sigHeader) {
        try {
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);
            return Webhook.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new PaymentException("Invalid webhook signature", e);
        }
    }

    private Session deserializeSession(Event event) {
        String rawJson = event.getDataObjectDeserializer().getRawJson();
        if (rawJson == null) {
            throw new PaymentException("Could not deserialize Stripe session");
        }
        try {
            return ApiResource.GSON.fromJson(rawJson, Session.class);
        } catch (Exception e) {
            throw new PaymentException("Could not parse Stripe session: " + e.getMessage());
        }
    }

    private Long parsePaymentId(String paymentIdStr) {
        try {
            return Long.parseLong(paymentIdStr);
        } catch (NumberFormatException e) {
            throw new PaymentException("Invalid paymentId in metadata: " + paymentIdStr);
        }
    }
    private String buildGameTitlesList(List<Game> games) {
        return games.stream()
                .map(Game::getTitle)
                .collect(Collectors.joining(", "));
    }

    private String buildConfirmationEmailBody(Users buyer, String gameTitles) {
        return "Hi " + buyer.getUsername() + ",\n\n" +
                "Your purchase was successful! These games are now in your library:\n\n" +
                gameTitles + "\n\nThanks for using GameSales!";
    }
    private void distributePaymentSafely(Payment payment, Long paymentId) {
        try {
            distributionService.distributePayment(payment);
        } catch (Exception e) {
            log.error("Distribution failed, will retry later | paymentId={} | error={}",
                    paymentId, e.getMessage());
        }
    }
    private String resolveChargeId(Session session) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
            return paymentIntent.getLatestCharge();
        } catch (StripeException e) {
            throw new PaymentException("Could not resolve charge ID from payment intent: " + e.getMessage());
        }
    }
}