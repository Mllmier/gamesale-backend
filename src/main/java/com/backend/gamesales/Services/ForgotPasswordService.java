package com.backend.gamesales.Services;

import com.backend.gamesales.Config.ForgotPasswordProperties;
import com.backend.gamesales.Dto.Records.MailBody;
import com.backend.gamesales.Model.ForgotPassword;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Repository.ForgotPasswordRepository;
import com.backend.gamesales.Repository.UsersRepository;
import com.backend.gamesales.Utils.PasswordValidator;
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
    private final EmailService emailService;
    private final ForgotPasswordProperties forgotPasswordProperties;


    public void generateAndSendOtp(String email){
        Date now = new Date();
        Users users=usersRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Email not found"));

        long windowMillis = forgotPasswordProperties.getWindowHours() * 60L * 60 * 1000;
        int maxRequests = forgotPasswordProperties.getMaxResetRequests();
        long otpExpiration = forgotPasswordProperties.getOtpExpirationMinutes() * 60L * 1000;

        ForgotPassword forgotPassword = forgotPasswordRepository
                .findByUsers(users)
                .orElseGet(() -> {
                    ForgotPassword newFp = new ForgotPassword();
                    newFp.setUsers(users);
                    newFp.setResetRequestCount(0);
                    newFp.setVerified(false);
                    newFp.setLastRequestDate(null);
                    return newFp;
                });

        boolean windowExpired = forgotPassword.getLastRequestDate() == null
                || (now.getTime() - forgotPassword.getLastRequestDate().getTime()) >= windowMillis;

        if (windowExpired) {
            forgotPassword.setResetRequestCount(0);
        }

        if (forgotPassword.getResetRequestCount() >= maxRequests) {
            throw new RuntimeException(
                    "Maximum reset requests reached. Please wait 24 hours before trying again."
            );
        }

        Integer otp = new SecureRandom().nextInt(900000) + 100000;
        forgotPassword.setOtp(otp);
        forgotPassword.setExpirationTime(new Date(now.getTime() + otpExpiration));
        forgotPassword.setVerified(false);
        forgotPassword.setResetRequestCount(forgotPassword.getResetRequestCount() + 1);
        forgotPassword.setLastRequestDate(now);

        forgotPasswordRepository.save(forgotPassword);
        emailService.sendSimpleMessage(new MailBody(
                email,
                "Recuperación de contraseña - GameSales",
                "Tu código OTP es: " + otp + "\n\nExpira en 10 minutos."
        ));

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

        fp.setVerified(true);
        forgotPasswordRepository.save(fp);
        return "OTP_VALID";

    }
    public void changePassword(String email, String newPassword) {

        if (!PasswordValidator.isValid(newPassword)) {
            throw new RuntimeException(
                    "Password must be at least 12 characters long and include " +
                            "uppercase, lowercase, a number and a special character"
            );
        }
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        ForgotPassword fp = forgotPasswordRepository
                .findByUsers(user)
                .orElseThrow(() -> new RuntimeException("OTP not verified"));

        if (!fp.isVerified()) {
            throw new RuntimeException("OTP not verified");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
        emailService.sendSimpleMessage(new MailBody(
                email,
                "Contraseña actualizada - GameSales",
                "Hola,\n\nTu contraseña fue actualizada exitosamente.\n\n" +
                        "Si no fuiste tú, contacta soporte inmediatamente.\n\nGameSales"
        ));
        fp.setOtp(null);
        fp.setVerified(false);
        fp.setExpirationTime(null);
        forgotPasswordRepository.save(fp);
    }


}
