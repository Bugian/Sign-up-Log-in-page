package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Long id;
    private String name;

    // Enum for predefined roles
    @Getter
    public enum RoleType {
        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN"),
        MODERATOR("ROLE_MODERATOR");

        private final String value;

        RoleType(String value) {
            this.value = value;
        }

        // Add method to find RoleType by value
        public static RoleType fromValue(String value) {
            for (RoleType roleType : values()) {
                if (roleType.value.equals(value)) {
                    return roleType;
                }
            }
            throw new IllegalArgumentException("Unknown role: " + value);
        }
    }

    // Constructor for new roles using RoleType
    public Role(RoleType roleType) {
        this.id = null;
        this.name = roleType.getValue();
    }

    // Helper method to format role names
    private String formatRoleName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }

        // Convert to uppercase and ensure it starts with ROLE_
        String formattedName = name.toUpperCase().trim();
        if (!formattedName.startsWith("ROLE_")) {
            formattedName = "ROLE_" + formattedName;
        }
        return formattedName;
    }

    // Utility method to check role type
    public boolean isRole(RoleType roleType) {
        return name != null && name.equals(roleType.getValue());
    }

    // Get RoleType from current role
    public RoleType getRoleType() {
        try {
            return RoleType.fromValue(name);
        } catch (IllegalArgumentException e) {
            return null; // Return null for custom roles
        }
    }

    // Factory methods for common roles
    public static Role user() {
        return new Role(RoleType.USER);
    }

    public static Role admin() {
        return new Role(RoleType.ADMIN);
    }

    public static Role moderator() {
        return new Role(RoleType.MODERATOR);
    }

    // Validation method
    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        if (!name.startsWith("ROLE_")) {
            throw new IllegalArgumentException("Role name must start with 'ROLE_'");
        }
    }

    // Override toString to provide a cleaner output
    @Override
    public String toString() {
        return name;
    }
}
