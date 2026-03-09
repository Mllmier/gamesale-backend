package com.backend.gamesales.Repository;

import com.backend.gamesales.Model.ForgotPassword;
import com.backend.gamesales.Model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword,Long> {

    Optional<ForgotPassword> findByOtpAndUsers(Integer otp, Users users);
    Optional<ForgotPassword> findByUsers(Users users);
    void deleteByUsers(Users users);
}
