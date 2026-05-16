package com.backend.gamesales.Dto.Request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotBlank(message = "The comment cannot be empty.")
        String comment,
        @NotNull(message = "Rating is mandatory")
        @Min(value = 1, message = "The minimum rating is 1")
        @Max(value = 5, message = "The maximum rating is 5")
        Integer rating
) {}