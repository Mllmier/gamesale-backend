package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Response.ReviewResponse;
import com.backend.gamesales.Dto.Request.ReviewRequest;
import com.backend.gamesales.Services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(id, userDetails.getUsername(), request));
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponse>> getReviews(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewsByGameId(id));
    }
}
