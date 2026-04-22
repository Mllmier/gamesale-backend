package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.ProfileRequest;
import com.backend.gamesales.Dto.ProfileResponse;
import com.backend.gamesales.Dto.SellerRequest;
import com.backend.gamesales.Dto.SellerResponse;
import com.backend.gamesales.Model.Profile;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileRequest request) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }

    @PatchMapping(value = "/avatar", consumes = "multipart/form-data")
    public ResponseEntity<ProfileResponse> updateAvatar(
            Authentication authentication,
            @RequestParam("image") MultipartFile image) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.updateAvatar(user, image));
    }

    @PatchMapping("/avatar/select")
    public ResponseEntity<ProfileResponse> selectAvatar(
            Authentication authentication,
            @RequestParam("avatarUrl") String avatarUrl) {
        Users user = (Users) authentication.getPrincipal();
        return ResponseEntity.ok(profileService.selectAvatar(user, avatarUrl));
    }

    @GetMapping("/avatars/defaults")
    public ResponseEntity<List<String>> getDefaultAvatars() {
        return ResponseEntity.ok(profileService.getDefaultAvatars());
    }
}
