package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Request.ChangePasswordRequest;
import com.backend.gamesales.Dto.Request.ProfileRequest;
import com.backend.gamesales.Dto.Response.ProfileResponse;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    private final PasswordEncoder passwordEncoder;

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

    @PatchMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new RuntimeException("Las contraseñas nuevas no coinciden");
        }
        Users user = (Users) authentication.getPrincipal();
        profileService.changePasswordAuthenticated(
                user, request.currentPassword(), request.newPassword(), passwordEncoder);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada exitosamente"));
    }

}
