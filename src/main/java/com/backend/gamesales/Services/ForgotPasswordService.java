package com.backend.gamesales.Services;

import com.backend.gamesales.Model.ForgotPassword;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.ForgotPasswordRepository;
import com.backend.gamesales.Repository.UsersRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final ForgotPasswordRepository forgotPasswordRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public void generateAndSendOtp(String email){
        Users users=usersRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Email not found"));

        ForgotPassword forgotPassword=forgotPasswordRepository
                .findByUsers(users)
                .orElse(
                        ForgotPassword.builder()
                        .users(users)
                        .build());

        Integer otp=new SecureRandom().nextInt(900000) + 100000;
        forgotPassword.setOtp(otp);
        forgotPassword.setExpirationTime(
                new Date(System.currentTimeMillis() + 10 * 60 * 1000)
        );

        forgotPasswordRepository.save(forgotPassword);

    }

    public String verifyOtp(Integer otp,String email){
        Users users =usersRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Email not found"));

        ForgotPassword fp = forgotPasswordRepository
                .findByOtpAndUsers(otp, users)
                .orElseThrow(() -> new RuntimeException("OTP invalid"));

        if (fp.getExpirationTime().before(new Date())) {
            throw new RuntimeException("OTP expired");
        }

        forgotPasswordRepository.delete(fp);
        return "OTP_VALID";

    }
    public void changePassword(String email, String newPassword) {
        usersRepository.updatePassword(
                email,
                passwordEncoder.encode(newPassword)
        );
    }

}
