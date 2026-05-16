package com.backend.gamesales.Dto.Response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record InvoiceResponse(Long id,
                              String invoiceNumber,
                              String gameTitle,
                              BigDecimal amount,
                              String currency,
                              String buyerEmail,
                              String buyerFullName,
                              LocalDateTime issuedAt) {
}
