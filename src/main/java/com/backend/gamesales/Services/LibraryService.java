package com.backend.gamesales.Services;


import com.backend.gamesales.Dto.Response.GameResponse;
import com.backend.gamesales.Dto.Response.GameResponse;
import com.backend.gamesales.Dto.Response.UserGameResponse;
import com.backend.gamesales.Exceptions.PaymentException;
import com.backend.gamesales.Model.UserGame;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.UserGameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final UserGameRepository userGameRepository;

    public Page<UserGameResponse> getLibrary(Users user, Pageable pageable) {
        log.info("Fetching library | userId: {} | page: {}",
                user.getId(), pageable.getPageNumber());

        return userGameRepository
                .findByUserIdOrderByPurchasedAtDesc(user.getId(), pageable)
                .map(this::toResponse);
    }

    public boolean ownsGame(Users user, Long gameId) {
        return userGameRepository.existsByUserIdAndGameId(user.getId(), gameId);
    }

    private UserGameResponse toResponse(UserGame ug) {
        return UserGameResponse.builder()
                .id(ug.getId())
                .gameId(ug.getGame().getId())
                .gameTitle(ug.getGame().getTitle())
                .gameImageUrl(ug.getGame().getImageUrl())
                .developer(ug.getGame().getDeveloper())
                .price(ug.getGame().getPrice())
                .purchasedAt(ug.getPurchasedAt())
                .build();
    }
}