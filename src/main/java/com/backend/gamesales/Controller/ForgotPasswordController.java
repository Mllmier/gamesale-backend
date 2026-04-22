package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.Records.ChangePassword;
import com.backend.gamesales.Dto.Records.VerifyOtpRequest;
import com.backend.gamesales.Services.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/forgot-password")
@CrossOrigin("*")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/verify-mail/{email}")
    public ResponseEntity<String> verifyMail(@PathVariable String email){
        forgotPasswordService.generateAndSendOtp(email);
        return ResponseEntity.ok("OTP send to your email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
    @RequestBody
    VerifyOtpRequest request){

    String response=forgotPasswordService.verifyOtp(
            request.otp(),
            request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
    @RequestBody ChangePassword request){

        if(!request.password().equals(request.repeatPassword())){
            return ResponseEntity.badRequest().body("Passwords do not match");
        }
        forgotPasswordService.changePassword(
                request.email(),
                request.password()
        );

        return ResponseEntity.ok("Password updated successfully");
    }


}
