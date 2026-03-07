package com.backend.gamesales.Controller;

import com.backend.gamesales.Dto.LoginRequest;
import com.backend.gamesales.Dto.RefreshTokenRequest;
import com.backend.gamesales.Dto.RegisterRequest;
import com.backend.gamesales.Model.RefreshToken;
import com.backend.gamesales.Model.Users;
import com.backend.gamesales.Services.AuthService;
import com.backend.gamesales.Services.JwtService;
import com.backend.gamesales.Services.RefreshTokenService;
import com.backend.gamesales.Utils.ErrorResponseBuilder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    private final JwtService jwtService;

    public AuthController(AuthService authService,JwtService jwtService){
        this.authService=authService;
        this.jwtService=jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        try{
            Users users=authService.register(request);

            Map<String,Object> extraClaims=new HashMap<>();
            extraClaims.put("email", users.getEmail());
            extraClaims.put("name",users.getName());
            String jwtToken=jwtService.generateToken(extraClaims,users);
            RefreshToken refreshToken =refreshTokenService.createRefreshToken(users);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(buildTokenResponse(jwtToken,refreshToken.getToken(),users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponseBuilder.buildErrorResponse(
                            e.getMessage(),HttpStatus.BAD_REQUEST
                    ));
        }
    }


    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){

        try{
            Users users=authService.authenticate(request.getEmail(),request.getPassword());
            refreshTokenService.deleteByUser(users);

            Map<String,Object>extraClaims=new HashMap<>();
            extraClaims.put("email",users.getEmail());
            extraClaims.put("name",users.getName());
            extraClaims.put("role", users.getRole());

            String jwtToken=jwtService.generateToken(extraClaims,users);
                RefreshToken refreshToken=refreshTokenService.createRefreshToken(users);
                return ResponseEntity.ok(
                        buildTokenResponse(jwtToken,refreshToken.getToken(),users)
                );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponseBuilder.buildErrorResponse(
                            e.getMessage(),HttpStatus.UNAUTHORIZED
                    ));
        }

    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new RuntimeException("Refresh token invalid"));

            if (refreshTokenService.isTokenExpired(refreshToken)) {
                refreshTokenService.deleteByUser(refreshToken.getUsers());
                throw new RuntimeException("Token is expired");
            }
            RefreshToken newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);
            Users users = refreshToken.getUsers();
            Map<String, Object> extraClaims = new HashMap<>();
            extraClaims.put("email", users.getEmail());
            extraClaims.put("name", users.getName());
            extraClaims.put("role", users.getRole());

            String newJwt = jwtService.generateToken(extraClaims, users);

            return ResponseEntity.ok(
                    buildTokenResponse(newJwt, newRefreshToken.getToken(), users)
            );
        } catch (RuntimeException e) {
            return buildErrorResponse(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.findByToken(request.getRefreshToken())
                .ifPresent(token -> refreshTokenService.deleteByUser(token.getUsers()));
        return ResponseEntity.ok(Map.of("message", "Logout exitoso"));
    }

    private Map<String, Object> buildTokenResponse(String token, String refreshToken, Users users) {
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("refreshToken", refreshToken);
        response.put("User", buildUsuarioResponse(users));
        return response;
    }
    private Map<String, Object> buildUsuarioResponse(Users users) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", users.getId());
        userMap.put("email", users.getEmail());
        userMap.put("Name", users.getName());
        userMap.put("LastName", users.getLastname());
        userMap.put("role", users.getRole());


        return userMap;
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("ERROR", message);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.status(status).body(response);
    }
}
