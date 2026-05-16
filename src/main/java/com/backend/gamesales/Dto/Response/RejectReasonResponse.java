package com.backend.gamesales.Dto.Response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RejectReasonResponse {
    private String message;
    private String reason;
}
