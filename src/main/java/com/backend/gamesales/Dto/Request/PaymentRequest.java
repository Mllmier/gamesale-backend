package com.backend.gamesales.Dto.Request;

import com.backend.gamesales.Model.Enums.PaymentMethods;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentRequest {

    @NotBlank(message = "The success URL is required")
    private String successUrl;

    @NotBlank(message = "The cancellation URL is required.")
    private String cancelUrl;
}
