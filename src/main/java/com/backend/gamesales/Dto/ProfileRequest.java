package com.backend.gamesales.Dto;

import lombok.*;


import jakarta.validation.constraints.Size;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

    private String firstName;
    private String lastName;
    @Size(max = 300, message = "La bio no puede superar 300 caracteres")
    private String bio;
    private String country;
    private String phoneNumber;
}
