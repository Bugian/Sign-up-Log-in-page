package org.example.service;

import org.example.exception.AuthException;
import org.example.model.Role;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) throws AuthException {
        return userRepository.findById(id)
                .orElseThrow(() -> new AuthException("User not found with id: " + id));
    }

    // Validation methods
    private void validateNewUser(User user) throws AuthException {
        validateUsername(user.getUsername(), null);
        validatePassword(user.getPassword());
        if (user.getEmail() != null) {
            validateEmail(user.getEmail());
        }
    }

    private void validateUsername(String username, Long excludeUserId) throws AuthException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("Username cannot be empty");
        }
        if (username.length() < 3 || username.length() > 50) {
            throw new AuthException("Username must be between 3 and 50 characters");
        }

        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent() &&
                (excludeUserId == null || !existingUser.get().getId().equals(excludeUserId))) {
            throw new AuthException("Username already exists");
        }
    }

    private void validateEmail(String email) throws AuthException {
        if (email != null && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new AuthException("Invalid email format");
        }
    }

    private void validatePassword(String password) throws AuthException {
        if (password == null || password.trim().isEmpty()) {
            throw new AuthException("Password cannot be empty");
        }
        if (password.length() < 8) {
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

    // Utility methods
    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    // Create operations
    public User createUser(User user) throws AuthException {
        validateNewUser(user);
        user.setPassword(hashPassword(user.getPassword()));
        if (user.getRoles().isEmpty()) {
            user.addRole(Role.user());
        }
        return userRepository.save(user);
    }

    // Read operations
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public User getUserByUsername(String username) throws AuthException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found with username: " + username));
    }

    // Update operations
    public User updateUser(Long id, User userDetails) throws AuthException {
        User user = getUserById(id);

        if (userDetails.getUsername() != null && !userDetails.getUsername().isEmpty()) {
            validateUsername(userDetails.getUsername(), id);
            user.setUsername(userDetails.getUsername());
        }

        if (userDetails.getEmail() != null) {
            validateEmail(userDetails.getEmail());
            user.setEmail(userDetails.getEmail());
        }

        return userRepository.save(user);
    }

    // Delete operations
    public void deleteUser(Long id) throws AuthException {
        if (!userRepository.existsById(id)) {
            throw new AuthException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    // Role management
    public User addRoleToUser(Long userId, Role.RoleType roleType) throws AuthException {
        User user = getUserById(userId);
        Role role = new Role(roleType);
        user.addRole(role);
        return userRepository.save(user);
    }

    public User removeRoleFromUser(Long userId, Role.RoleType roleType) throws AuthException {
        User user = getUserById(userId);
        Role role = new Role(roleType);
        user.removeRole(role);
        return userRepository.save(user);
    }

    // Account status management
    public User enableUser(Long id) throws AuthException {
        User user = getUserById(id);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public User disableUser(Long id) throws AuthException {
        User user = getUserById(id);
        user.setEnabled(false);
        return userRepository.save(user);
    }

    public User lockUser(Long id) throws AuthException {
        User user = getUserById(id);
        user.setAccountNonLocked(false);
        return userRepository.save(user);
    }

    public User unlockUser(Long id) throws AuthException {
        User user = getUserById(id);
        user.setAccountNonLocked(true);
        return userRepository.save(user);
    }

    // Password management
    public void changePassword(Long userId, String oldPassword, String newPassword) throws AuthException {
        User user = getUserById(userId);
        if (!verifyPassword(oldPassword, user.getPassword())) {
            throw new AuthException("Current password is incorrect");
        }
        validatePassword(newPassword);
        user.setPassword(hashPassword(newPassword));
        userRepository.save(user);
    }

    public void resetPassword(Long userId, String newPassword) throws AuthException {
        User user = getUserById(userId);
        validatePassword(newPassword);
        user.setPassword(hashPassword(newPassword));
        user.setCredentialsNonExpired(true);
        userRepository.save(user);
    }
}


