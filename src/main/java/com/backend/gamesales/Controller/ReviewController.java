package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Records.ReviewResponse;
import com.backend.gamesales.Dto.Records.ReviewRequest;
import com.backend.gamesales.Services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/games")
@CrossOrigin("*")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody  ReviewRequest request){
        ReviewResponse response =reviewService.createReview(
                id,
                userDetails.getUsername(),
                request
        );
        return ResponseEntity.ok(response);
    }
}
