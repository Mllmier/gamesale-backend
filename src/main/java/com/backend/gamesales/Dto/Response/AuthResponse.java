package com.backend.gamesales.Dto.Response;

import com.backend.gamesales.Model.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String email;
    private Role  role;
    private Long userId;
    private String name;

    public AuthResponse(String token, String refreshToken, Role role,String email,Long userId,String name){
        this.token = token;
        this.refreshToken = refreshToken;
        this.email = email;
        this.role = role;
        this.userId = userId;
        this.name = name;
    }
}
