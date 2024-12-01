package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.regex.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    private String confirmPassword;

    // Email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    // Username regex pattern (alphanumeric and underscore, 3-30 characters)
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,30}$"
    );

    // Password requirements
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 100;

    public void validate() throws IllegalArgumentException {
        validateUsername();
        validateEmail();
        validatePassword();
        validatePasswordMatch();
    }

    private void validateUsername() {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must be 3-30 characters long and contain only letters, numbers, and underscores"
            );
        }
    }

    private void validateEmail() {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private void validatePassword() {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long"
            );
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must not exceed " + MAX_PASSWORD_LENGTH + " characters"
            );
        }

        // Check for password complexity
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        //Validarea complexitatii parolei
        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter, " +
                            "one lowercase letter, one number, and one special character"
            );
        }

        throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter, " +
                        "one lowercase letter, one number, and one special character"
        );
    }

    private void validatePasswordMatch() {
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}

