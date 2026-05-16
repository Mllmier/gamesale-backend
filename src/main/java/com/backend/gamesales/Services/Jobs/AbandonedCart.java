package com.backend.gamesales.Services.Jobs;

import com.backend.gamesales.Model.Cart;
import com.backend.gamesales.Model.Enums.CartStatus;
import com.backend.gamesales.Repository.CartRepository;
import com.backend.gamesales.Services.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbandonedCart {
    private final CartRepository      cartRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 10 * * *")
    @Transactional
    public void execute() {
        log.info("[AbandonedCartJob] Iniciando...");

        LocalDateTime threshold = LocalDateTime.now().minusDays(3);

        List<Cart> abandonedCarts = cartRepository
                .findAbandonedCarts(CartStatus.ACTIVE, threshold);

        if (abandonedCarts.isEmpty()) {
            log.info("[AbandonedCartJob] No hay carritos abandonados.");
            return;
        }

        for (Cart cart : abandonedCarts) {
            processAbandonedCart(cart);
        }

        log.info("[AbandonedCartJob] {} carritos procesados.", abandonedCarts.size());
    }

    private void processAbandonedCart(Cart cart) {
        try {
            Long userId  = cart.getUser().getId();
            int itemCount = cart.getItems().size();

            notificationService.notifyAbandonedCart(userId, itemCount);

            cart.setStatus(CartStatus.ABANDONED);
            cartRepository.save(cart);

            log.info("[AbandonedCartJob] Carrito procesado | userId: {} | items: {}", userId, itemCount);

        } catch (Exception e) {
            log.error("[AbandonedCartJob] Error | cartId: {} | error: {}",
                    cart.getId(), e.getMessage());
        }
    }
}
