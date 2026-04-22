package com.backend.gamesales.Dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileResponse {
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String avatar;
    private String country;
    private String phoneNumber;

}
