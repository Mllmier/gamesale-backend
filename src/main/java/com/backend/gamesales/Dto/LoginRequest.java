package com.backend.gamesales.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message="Email is required")
    @Email(message="The email must be valid")

    private String email;
    private String password;
    @NotBlank(message="The password is required")

    public String getEmail(){
        return email;
    }

}
