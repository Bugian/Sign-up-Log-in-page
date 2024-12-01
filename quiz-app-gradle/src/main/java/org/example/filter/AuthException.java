package org.example.filter;

public class AuthException extends Exception {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    // Common authentication error types
    public static class InvalidCredentialsException extends AuthException {
        public InvalidCredentialsException() {
            super("Invalid username or password");
        }
    }

    public static class UserNotFoundException extends AuthException {
        public UserNotFoundException(String username) {
            super("User not found: " + username);
        }
    }

    public static class UserAlreadyExistsException extends AuthException {
        public UserAlreadyExistsException(String username) {
            super("User already exists: " + username);
        }
    }

    public static class TokenExpiredException extends AuthException {
        public TokenExpiredException() {
            super("Authentication token has expired");
        }
    }

    public static class InvalidTokenException extends AuthException {
        public InvalidTokenException() {
            super("Invalid authentication token");
        }
    }

    public static class UnauthorizedException extends AuthException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }
}
