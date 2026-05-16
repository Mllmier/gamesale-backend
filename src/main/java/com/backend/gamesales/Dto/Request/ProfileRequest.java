package com.backend.gamesales.Dto.Request;

import lombok.*;


import jakarta.validation.constraints.Size;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {

    private String firstName;
    private String email;
    private String lastName;
    @Size(max = 300, message = "The bio cannot exceed 300 characters")
    private String bio;
    private String country;
    private String phoneNumber;
}
