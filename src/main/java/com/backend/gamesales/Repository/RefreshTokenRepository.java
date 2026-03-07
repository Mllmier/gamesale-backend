package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.RefreshToken;
import com.backend.gamesales.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken,Long > {

    Optional <RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUsers(Users users);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.users = :user")
    void deleteByUser(@Param("user")Users users);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredToken();

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.users.id = :userId")
    void deleteByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now")
    void deleteAllByExpiryDateBefore(Instant now);

}
