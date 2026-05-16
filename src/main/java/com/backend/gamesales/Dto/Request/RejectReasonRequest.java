package com.backend.gamesales.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectReasonRequest {
    @NotBlank(message = "The motive is required")
    private String reason;

}
