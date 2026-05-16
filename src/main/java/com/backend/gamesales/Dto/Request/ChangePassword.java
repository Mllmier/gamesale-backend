package com.backend.gamesales.Dto.Request;

public record ChangePassword(String email,String password, String repeatPassword) {
}
