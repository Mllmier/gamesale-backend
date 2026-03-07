package com.backend.gamesales.Services;

import com.backend.gamesales.Model.RefreshToken;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {

    @Value("${app.jwt.refreshExpirationMs}")
    private long refreshDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public RefreshToken createRefreshToken(Users users) {
        refreshTokenRepository.deleteByUserId(users.getId());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsers(users);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshDurationMs));
        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken rotateRefreshToken(RefreshToken oldToken) {
        refreshTokenRepository.deleteById(oldToken.getId());
        refreshTokenRepository.flush();

        return createRefreshToken(oldToken.getUsers());
    }

    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }

    public boolean isTokenExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }

    public void deleteByUser(Users user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
