package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Response.NotificationResponse;
import com.backend.gamesales.Exceptions.PaymentException;
import com.backend.gamesales.Model.Enums.NotificationType;
import com.backend.gamesales.Model.Notification;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponseDTO);
    }

    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new PaymentException(
                        "Notification not found: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new PaymentException("Access denied: not your notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, Pageable.unpaged())
                .forEach(n -> n.setRead(true));

        log.info("All notifications marked as read | userId: {}", userId);
    }

    @Async
    public void notifyPurchaseSucceeded(Long buyerId, String gameTitles) {
        saveNotification(
                buyerId,
                NotificationType.PAYMENT_SUCCEEDED,
                "Payment successful",
                "Your purchase of " + gameTitles + " was completed. Enjoy your game!"
        );
        log.info("Purchase succeeded notification sent | buyerId: {}", buyerId);
    }

    @Async
    public void notifyGameSold(Long sellerId, String gameTitle) {
        saveNotification(
                sellerId,
                NotificationType.GAME_SOLD,
                "You sold a game",
                "Your game " + gameTitle + " was just purchased."
        );
        log.info("Game sold notification sent | sellerId: {}", sellerId);
    }

    @Async
    public void notifyPaymentFailed(Long buyerId, String gameTitle) {
        saveNotification(
                buyerId,
                NotificationType.PAYMENT_FAILED,
                "Payment failed",
                "Your payment for " + gameTitle + " could not be processed. Please try again."
        );
    }


    @Async
    public void notifyUser(Users user, String message) {
        saveNotification(
                user.getId(),
                NotificationType.GAME_PURCHASED,
                "Notification",
                message
        );
    }

    @Async
    public void notifyWishlistDiscount(Users user, String gameTitle, Double discountPercentage) {
        saveNotification(
                user.getId(),
                NotificationType.DISCOUNT_ACTIVATED,
                "Game on your wishlist is on sale!",
                String.format("%s now has a %.0f%% discount. Don't miss it!", gameTitle, discountPercentage)
        );
    }

    @Async
    public void notifyAbandonedCart(Long userId, int itemCount) {
        saveNotification(
                userId,
                NotificationType.ABANDONED_CART,
                "You left something behind!",
                String.format(
                        "You have %d game%s waiting in your cart. Complete your purchase before they're gone!",
                        itemCount, itemCount > 1 ? "s" : ""
                )
        );
        log.info("Abandoned cart notification sent | userId: {} | items: {}", userId, itemCount);
    }

    private void saveNotification(Long userId, NotificationType type,
                                  String title, String message) {
        notificationRepository.save(
                Notification.builder()
                        .userId(userId)
                        .type(type)
                        .title(title)
                        .message(message)
                        .build()
        );
    }

    private NotificationResponse toResponseDTO(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.getRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}