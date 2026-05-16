package com.backend.gamesales.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.gamesales.Dto.Request.PromotionRequest;
import com.backend.gamesales.Dto.Response.PromotionResponse;
import com.backend.gamesales.Model.PromotionProposal;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.PromotionRequestService;
import com.backend.gamesales.Services.PromotionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;
    private final PromotionRequestService promotionRequestService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody PromotionRequest request,
            Authentication authentication) {
        Users seller = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(promotionService.createPromotion(request, seller)
        );
    }
    @PostMapping("/proposal")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<PromotionProposal> createProposal(
            @RequestBody @Valid PromotionRequest request,
            Authentication authentication) {
        Users admin = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(
                promotionRequestService.createProposal(request, admin)
        );
    }

    @PutMapping("/proposal/{id}/accept")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Map<String, String>> acceptProposal(
            @PathVariable Long id,
            Authentication authentication) {
        Users seller = (Users) authentication.getPrincipal();
        promotionRequestService.accept(id, seller);
        return ResponseEntity.ok(Map.of("message", "Proposal accepted"));
    }

    @PutMapping("/proposal/{id}/reject")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Map<String, String>> rejectProposal(
            @PathVariable Long id,
            Authentication authentication) {
        Users seller = (Users) authentication.getPrincipal();
        promotionRequestService.reject(id, seller);
        return ResponseEntity.ok(Map.of("message", "Proposal rejected"));
    }

    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> getActivePromotions() {
        return ResponseEntity.ok(promotionService.getActivePromotions());
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<PromotionResponse> getPromotionByGame(
            @PathVariable Long gameId) {
        return ResponseEntity.ok(
                promotionService.getPromotionByGame(gameId)
        );
    }
}
