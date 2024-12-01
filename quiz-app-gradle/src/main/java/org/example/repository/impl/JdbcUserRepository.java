package org.example.repository.impl;

import org.example.config.DatabaseConfig;
import org.example.model.Role;
import org.example.model.User;
import org.example.repository.UserRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcUserRepository implements UserRepository {
    private final DataSource dataSource;

    public JdbcUserRepository(){
        this.dataSource = DatabaseConfig.getDataSource();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            return insert(user);
        }
        return update(user);
    }

    private User insert(User user) {
        String sql = "INSERT INTO users (username, password, email, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(5, user.isEnabled());
            ps.setBoolean(6, user.isAccountNonExpired());
            ps.setBoolean(7, user.isCredentialsNonExpired());
            ps.setBoolean(8, user.isAccountNonLocked());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getLong(1));
                    saveRoles(user);
                    return user;
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    private User update(User user) {
        String sql = "UPDATE users SET username = ?, email = ?, created_at = ?, " +
                "updated_at = ?, credentials_non_expired = ?, " +
                "account_non_locked = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setBoolean(3, user.isEnabled());
            ps.setBoolean(4, user.isAccountNonExpired());
            ps.setBoolean(5, user.isCredentialsNonExpired());
            ps.setBoolean(6, user.isAccountNonLocked());
            ps.setLong(7, user.getId());

            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating user failed, no rows affected.");
            }

            saveRoles(user);
            return user;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    user.setRoles(findUserRoles(user.getId()));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id", e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapUser(rs);
                    user.setRoles(findUserRoles(user.getId()));
                    return Optional.of(user);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = mapUser(rs);
                user.setRoles(findUserRoles(user.getId()));
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error finding all users", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if user exists", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if username exists", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if email exists", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user", e);
        }
    }

    @Override
    public void delete(User user) {
        if (user.getId() != null) {
            deleteById(user.getId());
        }
    }

    private void saveRoles(User user) throws SQLException {
        String deleteSql = "DELETE FROM user_roles WHERE user_id = ?";
        String insertSql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            // Delete existing roles
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setLong(1, user.getId());
                ps.executeUpdate();
            }

            // Insert new roles
            if (!user.getRoles().isEmpty()) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Role role : user.getRoles()) {
                        ps.setLong(1, user.getId());
                        ps.setLong(2, role.getId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    private Set<Role> findUserRoles(Long userId) throws SQLException {
        String sql = "SELECT r.* FROM roles r " +
                "JOIN user_roles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ?";

        Set<Role> roles = new HashSet<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(new Role(
                            rs.getLong("id"),
                            rs.getString("name")
                    ));
                }
            }
        }

        return roles;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setEnabled(rs.getBoolean("enabled"));
        user.setAccountNonExpired(rs.getBoolean("account_non_expired"));
        user.setCredentialsNonExpired(rs.getBoolean("credentials_non_expired"));
        user.setAccountNonLocked(rs.getBoolean("account_non_locked"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setLastLogin(createdAt.toLocalDateTime());
        }

        return user;
    }
}
