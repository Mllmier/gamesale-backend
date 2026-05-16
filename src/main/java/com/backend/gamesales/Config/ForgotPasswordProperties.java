package com.backend.gamesales.Config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "forgot-password")
public class ForgotPasswordProperties {
    private int maxResetRequests;
    private int windowHours;
    private int otpExpirationMinutes;
}
