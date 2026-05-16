package com.backend.gamesales.Events.Listeners;

import com.backend.gamesales.Events.PromotionActivatedEvent;
import com.backend.gamesales.Model.Promotion;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Model.Wishlist;
import com.backend.gamesales.Repository.WishlistRepository;
import com.backend.gamesales.Services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
@Component
@Slf4j
@RequiredArgsConstructor
public class WishlistNotificationListener {
    private final WishlistRepository wishlistRepository;
    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPromotionActivated(PromotionActivatedEvent event) {

        Long gameId = event.getGame().getId();
        String gameTitle = event.getGame().getTitle();
        Double discount = event.getPromotion().getDiscountPercentage();

        List<Wishlist> entries = wishlistRepository.findByGameId(gameId);

        if (entries.isEmpty()) return;

        entries.forEach(entry -> {
            try {
                notificationService.notifyWishlistDiscount(entry.getUser(), gameTitle, discount);
            } catch (Exception e) {
                log.error("Failed to notify user '{}': {}", entry.getUser().getId(), e.getMessage());
            }
        });
    }
}
