package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Request.ChangePassword;
import com.backend.gamesales.Dto.Request.VerifyOtpRequest;
import com.backend.gamesales.Services.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/forgot-password")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/verify-mail/{email}")
    public ResponseEntity<Map<String, String>> verifyMail(@PathVariable String email){
        forgotPasswordService.generateAndSendOtp(email);
        return ResponseEntity.ok(Map.of("message", "OTP send to your email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
    @RequestBody
    VerifyOtpRequest request){

    String response=forgotPasswordService.verifyOtp(
            request.otp(),
            request.email());
        return ResponseEntity.ok(Map.of("message", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
    @RequestBody ChangePassword request){

        if(!request.password().equals(request.repeatPassword())){
            return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
        }
        forgotPasswordService.changePassword(
                request.email(),
                request.password()
        );

        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }


}
