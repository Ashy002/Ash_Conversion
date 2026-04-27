package com.ashconversion.util;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    public static boolean isValidSearchTerm(String search) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public static boolean isValidPageNumber(String pageParam) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    // ------------------ Validation Email ------------------
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    // ------------------ Validation Username ------------------
    public boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) return false;
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    // ------------------ Validation Password ------------------
    public boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) return false;
        if (password.length() < MIN_PASSWORD_LENGTH) return false;

        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        return hasLetter && hasDigit;
    }

    public boolean passwordsMatch(String password, String confirmPassword) {
        if (password == null || confirmPassword == null) return false;
        return password.equals(confirmPassword);
    }

    // ------------------ Nettoyage ------------------
    public String sanitize(String input) {
        return input == null ? null : input.trim();
    }
}

