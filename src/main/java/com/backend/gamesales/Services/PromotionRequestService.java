package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Request.PromotionRequest;
import com.backend.gamesales.Events.PromotionActivatedEvent;
import com.backend.gamesales.Model.*;
import com.backend.gamesales.Model.Enums.DiscountType;
import com.backend.gamesales.Model.Enums.PromotionStatus;
import com.backend.gamesales.Model.Enums.RequestStatus;
import com.backend.gamesales.Repository.GameRepository;
import com.backend.gamesales.Repository.PromotionRepository;
import com.backend.gamesales.Repository.PromotionRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PromotionRequestService {

    private final PromotionRequestRepository requestRepository;
    private final GameRepository gameRepository;
    private final NotificationService notificationService;
    private  final PromotionRepository promotionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PromotionProposal createProposal(PromotionRequest request, Users admin) {
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));
        if (promotionRepository.existsActiveByGameId(game.getId(), LocalDateTime.now())) {
            throw new RuntimeException("This game already has an active promotion");
        }

        if (requestRepository.existsByGameIdAndStatus(game.getId(), RequestStatus.PENDING)) {
            throw new RuntimeException("This game already has a pending promotion proposal");
        }

        validateDates(request.getStartDate(), request.getEndDate());
        PromotionProposal entity = PromotionProposal.builder()
                .discountPercentage(request.getDiscountPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(RequestStatus.PENDING)
                .game(game)
                .seller(game.getSeller().getUser())
                .admin(admin)
                .build();

        PromotionProposal saved = requestRepository.save(entity);

        notificationService.notifyUser(
                saved.getSeller(),
                "New promotion proposal for your game: " + game.getTitle()
        );

        return saved;
    }
        @Transactional
        public void accept (Long id, Users seller){

            PromotionProposal request = requestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Not found"));
            if (!request.getSeller().getId().equals(seller.getId())) {
                throw new RuntimeException("Not your request");
            }
            if (promotionRepository.existsActiveByGameId(request.getGame().getId(), LocalDateTime.now())) {
                throw new RuntimeException("This game already has an active promotion");
            }


            request.setStatus(RequestStatus.APPROVED);
            requestRepository.save(request);

            Promotion promotion = Promotion.builder()
                    .title("Admin Promotion")
                    .description("Promotion proposed by admin")
                    .discountPercentage(request.getDiscountPercentage())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .game(request.getGame())
                    .seller(null)
                    .discountType(DiscountType.ADMIN)
                    .status(PromotionStatus.ACTIVE)
                    .build();

            Promotion saved = promotionRepository.save(promotion);

            eventPublisher.publishEvent(new PromotionActivatedEvent(this, saved, request.getGame()));

            notificationService.notifyUser(
                    request.getAdmin(),
                    "Seller accepted your promotion proposal"
            );
        }

    public void reject(Long id, Users seller) {
        PromotionProposal request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        if (!request.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("Not your request");
        }
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        notificationService.notifyUser(
                request.getAdmin(),
                "Seller rejected your promotion proposal"
        );
    }
    private void validateDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new RuntimeException("Start date must be before end date");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Start date cannot be in the past");
        }
    }

}