package com.backend.gamesales.Dto.Response;

import java.time.LocalDate;

public record ReviewResponse(Long id, String comment, Integer rating, LocalDate createdAt, String username) {
}
