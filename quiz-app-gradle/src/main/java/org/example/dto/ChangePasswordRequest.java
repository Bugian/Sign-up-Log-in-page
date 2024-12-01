package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChangePasswordRequest {
    // Getters and setters
    private String oldPassword;
    private String newPassword;

    // Validation method
    public void validate() {
        // Check for null or empty passwords
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Old password is required");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("New password is required");
        }

        // Validate new password requirements
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        // Check for at least one uppercase letter
        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("New password must contain at least one uppercase letter");
        }

        // Check for at least one lowercase letter
        if (!newPassword.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("New password must contain at least one lowercase letter");
        }

        // Check for at least one digit
        if (!newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("New password must contain at least one number");
        }

        // Check for at least one special character
        if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new IllegalArgumentException("New password must contain at least one special character");
        }

        // Check if old and new passwords are different
        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from old password");
        }
    }
}
