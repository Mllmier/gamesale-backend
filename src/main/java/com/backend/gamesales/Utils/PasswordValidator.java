package com.backend.gamesales.Utils;

public class PasswordValidator {
    public static boolean isValid(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }
}

