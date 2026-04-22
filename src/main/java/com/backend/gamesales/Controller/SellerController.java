package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.SellerRequest;
import com.backend.gamesales.Dto.SellerResponse;
import com.backend.gamesales.Dto.SellerStatusResponse;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.SellerService;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import com.backend.gamesales.Dto.SellerRequest;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller")
public class SellerController {


    private final SellerService sellerService;

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
            return ResponseEntity.status(401).body("User not authenticated");
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
    public ResponseEntity<String> approveSeller(@PathVariable Long id) {
        sellerService.approveSeller(id);
        return ResponseEntity.ok("Seller approved successfully");
    }

    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<String> rejectSeller(@PathVariable Long id) {
        sellerService.rejectSeller(id);
        return ResponseEntity.ok("Seller rejected successfully");
    }

}
