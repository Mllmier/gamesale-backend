package com.backend.gamesales.Config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class StripeConfig {
    @Value("${stripe.secret-key}")
    private String secretKey;

    @Value("${stripe.public-key}")
    private String publicKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.onboarding.return-url}")
    private String onboardingReturnUrl;

    @Value("${stripe.onboarding.refresh-url}")
    private String onboardingRefreshUrl;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = this.secretKey;
    }
}
