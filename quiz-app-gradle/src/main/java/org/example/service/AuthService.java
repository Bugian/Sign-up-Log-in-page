package org.example.service;

import com.jetbrains.exported.JBRApi;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.example.dto.LoginRequest;
import org.example.dto.SignupRequest;
import org.example.exception.AuthException;
import org.example.model.Role;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.example.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

@JBRApi.Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(){
        this.userRepository = new UserRepository() {
            @Override
            public List<User> findAll() {
                return List.of();
            }
            @Override
            public boolean existsById(Long id) {
                return false;
            }
            @Override
            public User save(User user) {
                return null;
            }
            @Override
            public Optional<User> findById(Long id) {
                return Optional.empty();
            }
            @Override
            public Optional<User> findByUsername(String username) {
                return Optional.empty();
            }
            @Override
            public boolean existsByUsername(String username) {
                return false;
            }
            @Override
            public boolean existsByEmail(String email) {
                return false;
            }
            @Override
            public void deleteById(Long id) {
            }
            @Override
            public void delete(User user) {
                // Implement delete logic if needed
            }

        };
        this.passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "";
            }
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return false;
            }
        };
        this.jwtUtil = new JwtUtil();
    }

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public String login(LoginRequest loginRequest) throws AuthException {
        // Validate request
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            throw new AuthException("Username and password are required");
        }

        // Find user
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(AuthException.InvalidCredentialsException::new);

        //Verificarea parolei cu PasswordEncoding
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AuthException.InvalidCredentialsException();
        }

        // Update last login
        user.updateLastLogin();
        userRepository.save(user);

        // Generate token
        return jwtUtil.generateToken(user);
    }

    public void register(SignupRequest signupRequest) throws AuthException {
        // Validate request
        if (!isValidEmail(signupRequest.getEmail())) {
            throw new AuthException("Invalid email format");
        }
        validateSignupRequest(signupRequest);

        // Check if username exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new AuthException.UserAlreadyExistsException("Username already registered");
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new AuthException.UserAlreadyExistsException("Email already registered");
        }

        try {
            // Create new user
            User user = User.builder()
                    .username(signupRequest.getUsername())
                    .password(passwordEncoder.encode(signupRequest.getPassword()))
                    .email(signupRequest.getEmail())
                    .enabled(true)
                    .accountNonExpired(true)
                    .credentialsNonExpired(true)
                    .accountNonLocked(true)
                    .build();

            // Add default role
            user.addRole(Role.user());

            // Save user
            userRepository.save(user);
        } catch (Exception e) {
            throw new AuthException("Error during user registration", e);
        }
    }

    public User authenticateToken(String token) throws AuthException {
        return validateToken(token);  // Now using the validateToken method
    }

    public String refreshToken(String refreshToken) throws AuthException {
        if (jwtUtil.validateToken(refreshToken)) {
            throw new AuthException.InvalidTokenException();
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException.UserNotFoundException(username));

        return jwtUtil.generateToken(user);
    }

    public User validateToken(String token) throws AuthException {
        if (token == null || token.isEmpty()) {
            throw new AuthException.UnauthorizedException("Token-ul nu poate fi gol");
        }

            try {
                if (jwtUtil.validateToken(token)) {
                    throw new AuthException.InvalidTokenException();
                }

                String username = jwtUtil.getUsernameFromToken(token);
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new AuthException.UserNotFoundException(username));

                if (!user.isEnabled()) {
                    throw new AuthException.UnauthorizedException("Account is disabled");
                }

                if (!user.isAccountNonLocked()) {
                    throw new AuthException.UnauthorizedException("Account is locked");
                }
                return user;
            } catch (ExpiredJwtException e) {
                throw new AuthException.TokenExpiredException();
            } catch (JwtException e) {
                throw new AuthException.UnauthorizedException("Invalid token format");
            }
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) throws AuthException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException.UserNotFoundException("User not found"));

        //verificarea parolei vechi
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }

        //validarea parolei noi
        validatePassword(newPassword);
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new AuthException("Parola noua trebuie sa fie diferita de cea veche");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }

    private void validateSignupRequest(SignupRequest request) throws AuthException {
        try {
            request.validate();
        } catch (IllegalArgumentException e) {
            throw new AuthException(e.getMessage());
        }
    }

    private void validatePassword(String password) throws AuthException {

        if (password == null || password.trim().isEmpty() || password.length() < 8) {
            throw new AuthException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new AuthException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new AuthException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new AuthException("Password must contain at least one number");
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new AuthException("Password must contain at least one special character");
        }
    }

    private boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }
}
