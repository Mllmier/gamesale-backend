package com.backend.gamesales.Dto;

import com.backend.gamesales.Model.Users;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Users users;

    public AuthResponse(String token, String refreshToken,Users users){
        this.token=token;
        this.refreshToken=refreshToken;
        this.users=users;
    }
}
