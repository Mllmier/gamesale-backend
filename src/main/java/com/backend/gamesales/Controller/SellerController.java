package com.backend.gamesales.Controller;

import java.util.Map;

import com.backend.gamesales.Dto.Response.RejectReasonResponse;
import com.backend.gamesales.Services.SellerOnboardingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.backend.gamesales.Dto.Request.RejectReasonRequest;
import com.backend.gamesales.Dto.Request.SellerRequest;
import com.backend.gamesales.Dto.Response.EarningResponse;
import com.backend.gamesales.Dto.Response.SellerResponse;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.SellerEarningService;
import com.backend.gamesales.Services.SellerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerService sellerService;
    private final SellerEarningService sellerEarningService;
    private final SellerOnboardingService sellerOnboardingService;


    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SellerResponse> requestSeller(
            Authentication authentication,
            @Valid @RequestBody SellerRequest request) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sellerService.requestSeller(request, user));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "User not authenticated"));
        }
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(sellerService.getSellerStatus(user));
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<SellerResponse> getProfile(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(sellerService.getSellerProfile(user));
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<SellerResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody SellerRequest request) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(sellerService.updateSellerProfile(user, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<SellerResponse> getPublicProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(sellerService.getPublicProfile(userId));
    }


    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<Map<String, String>> approveSeller(@PathVariable Long id) {
        sellerService.approveSeller(id);
        return ResponseEntity.ok(Map.of("message", "Seller approved successfully"));
    }

    @PostMapping("/onboarding")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<Map<String, String>> iniciarOnboarding(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        String url = sellerOnboardingService.iniciarOnboarding(user);
        return ResponseEntity.ok(Map.of("onboardingUrl", url));
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<RejectReasonResponse> rejectSeller(
            @PathVariable Long id,
            @Valid @RequestBody RejectReasonRequest request) {
        return ResponseEntity.ok(sellerService.rejectSeller(id, request.getReason()));
    }
    
    @GetMapping("/earnings")
    @PreAuthorize("hasAuthority('SELLER')")
    public ResponseEntity<EarningResponse> getEarnings(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(sellerEarningService.getEarnings(user));
    }
    @PatchMapping("/me/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deactivateMyAccount(@AuthenticationPrincipal UserDetails userDetails) {
        sellerService.deactivateMyAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }


}
