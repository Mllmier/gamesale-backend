package com.backend.gamesales.Dto.Response;

import com.backend.gamesales.Model.Enums.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}
