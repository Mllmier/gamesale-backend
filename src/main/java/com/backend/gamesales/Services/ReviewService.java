package com.backend.gamesales.Services;

import com.backend.gamesales.Dto.Records.ReviewRequest;
import com.backend.gamesales.Dto.Records.ReviewResponse;
import com.backend.gamesales.Model.Game;
import com.backend.gamesales.Model.Review;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.GameRepository;
import com.backend.gamesales.Repository.ReviewRepository;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final GameRepository gameRepository;
    private final UsersRepository usersRepository;


    public ReviewResponse createReview (Long gameId, String email, ReviewRequest request){

        Game game=gameRepository.findById(gameId)
                .orElseThrow(()-> new RuntimeException("Game not found"));

        Users users=usersRepository.findByEmail(email)
                .orElseThrow(()->new RuntimeException("User not found "));

        if(reviewRepository.existsByGameIdAndUserId(gameId,users.getId())){
            throw new RuntimeException("Yo have already reviewed this game");
        }

        Review review= Review.builder()
                .comment(request.comment())
                .rating(request.rating())
                .game(game)
                .user(users)
                .build();

        Review saved=reviewRepository.save(review);

        return toResponse(saved);
    }

    private ReviewResponse toResponse(Review review) {
        Users user = review.getUser();
        String username;

        if (user.getProfile() != null
                && user.getProfile().getFirstName() != null
                && user.getProfile().getLastName() != null) {
            username = user.getProfile().getFirstName()
                    + " " + user.getProfile().getLastName();
        } else {
            username = user.getEmail(); // fallback si no tiene perfil completo
        }

        return new ReviewResponse(
                review.getId(),
                review.getComment(),
                review.getRating(),
                review.getCreatedAt(),
                username
        );
    }
}
