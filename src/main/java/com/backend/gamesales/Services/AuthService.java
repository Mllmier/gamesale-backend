package com.backend.gamesales.Services;


import com.backend.gamesales.Dto.Response.AuthResponse;
import com.backend.gamesales.Dto.Request.RegisterRequest;
import com.backend.gamesales.Infrastructure.EmailSender;
import com.backend.gamesales.Model.Profile;
import com.backend.gamesales.Model.Enums.Role;
import com.backend.gamesales.Model.RefreshToken;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.UsersRepository;
import com.backend.gamesales.Utils.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailSender emailSenderService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse authenticate(String email, String password) {
        Users users = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User no found"));

        if (!passwordEncoder.matches(password, users.getPassword())) {
            throw new RuntimeException("incorrect password ");
        }

        return buildAuthResponse(users, refreshTokenService.createRefreshToken(users));
    }

    public AuthResponse register(RegisterRequest request) {
        if (usersRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("The email address is already registered");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("The passwords do not match");
        }

        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new RuntimeException(
                    "The password must be at least 8 characters long and include letters and numbers."
            );
        }

        Users users = new Users();
        users.setBirthday(request.getBirthday());
        users.setEmail(request.getEmail());
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users.setRole(request.getRole() != null ? request.getRole() : Role.CUSTOMER);

        Profile profile = new Profile();
        profile.setFirstName(request.getName());
        profile.setLastName(request.getLastName());
        profile.setUser(users);
        users.setProfile(profile);

        Users savedUser = usersRepository.save(users);

        try {
            emailSenderService.sendWelcomeEmail(savedUser.getEmail(), request.getName());
        } catch (Exception e) {
            log.error("Error triggering asynchronous email sending: {}", e.getMessage());
        }

        return buildAuthResponse(savedUser, refreshTokenService.createRefreshToken(savedUser));
    }

    public AuthResponse refreshToken(String token) {
        RefreshToken refreshToken = refreshTokenService.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refreshment token"));

        if (refreshTokenService.isTokenExpired(refreshToken)) {
            refreshTokenService.deleteByUser(refreshToken.getUsers());
            throw new RuntimeException("The token has expired");
        }

        RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
        return buildAuthResponse(newRefreshToken.getUsers(), newRefreshToken);
    }

    public void logout(String token) {
        refreshTokenService.findByToken(token)
                .ifPresent(rt -> refreshTokenService.deleteByUser(rt.getUsers()));
    }

    private AuthResponse buildAuthResponse(Users users, RefreshToken refresh) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("email", users.getEmail());
        extraClaims.put("role", users.getRole());

        String jwtToken = jwtService.generateToken(extraClaims, users);

        return AuthResponse.builder()
                .token(jwtToken)
                .refreshToken(refresh.getToken())
                .email(users.getEmail())
                .role(users.getRole())
                .userId(users.getId())
                .name(users.getProfile() != null ? users.getProfile().getFirstName() : null)
                .build();
    }
}

