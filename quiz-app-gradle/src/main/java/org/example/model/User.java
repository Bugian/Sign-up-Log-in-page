package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Cloneable {
    private Long id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime lastLogin;

    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    // Role management methods
    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }

   /* // Security helper methods
    public boolean isAdmin() {
        return hasRole(Role.RoleType.ADMIN);
    }

    public boolean isModerator() {
        return hasRole(Role.RoleType.MODERATOR);
    }

    public boolean hasRole(Role.RoleType roleType) {
        return roles.stream()
                .anyMatch(role -> role.isRole(roleType));
    }

    // Factory method
    public static User createUser(String username, String password) {
        return User.builder()
                .username(username)
                .password(password)
                .roles(new HashSet<>() {{ add(Role.user()); }})
                .build();
    }
    */

    // Update last login
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    @Override
    public User clone() throws CloneNotSupportedException {
        User cloned = (User) super.clone();
        cloned.roles = new HashSet<>(this.roles);
        return cloned;
    }
}
