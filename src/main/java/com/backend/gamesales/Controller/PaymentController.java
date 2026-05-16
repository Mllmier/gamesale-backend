package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Request.PaymentRequest;
import com.backend.gamesales.Dto.Response.PaymentResponse;
import com.backend.gamesales.Services.PaymentService;
import com.backend.gamesales.Services.WebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.gamesales.Model.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final WebhookService webhookService;


    @PostMapping("/checkout")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @AuthenticationPrincipal Users buyer) {

        log.info("Checkout requested | buyerId={}", buyer.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.initiateCartPayment(buyer));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            HttpServletRequest request,
            @RequestHeader("Stripe-Signature") String sigHeader) throws IOException {
        byte[] payload = request.getInputStream().readAllBytes();
        webhookService.processWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }

}