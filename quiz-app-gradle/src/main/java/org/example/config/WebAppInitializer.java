package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.example.repository.UserRepository;
import org.example.repository.impl.JdbcUserRepository;
import org.example.security.JwtUtil;
import org.example.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.util.TimeZone;

@WebListener
public class WebAppInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(WebAppInitializer.class);

    // Database configuration
    private static final String URL = System.getenv("DB_URL");
    private static final String USER = System.getenv("DB_USERNAME");
    private static final String PASSWORD = System.getenv("DB_PASSWORD");

    // Connection pool settings
    private static final int MAX_POOL_SIZE = 10;
    private static final int MIN_IDLE = 5;
    private static final int IDLE_TIMEOUT = 300000; // 5 minutes
    private static final int CONNECTION_TIMEOUT = 20000; // 20 seconds
    private static final int VALIDATION_TIMEOUT = 5000; // 5 seconds

    static {
        try {
            // Set default timezone
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            // Load MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load MySQL JDBC Driver", e);
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            validateConfigurations();

            // Initialize components
            DataSource dataSource = createDataSource();
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            JwtUtil jwtUtil = new JwtUtil(); // Using default constructor
            UserRepository userRepository = new JdbcUserRepository();
            AuthService authService = new AuthService(userRepository, passwordEncoder, jwtUtil);

            // Store in servlet context
            sce.getServletContext().setAttribute("dataSource", dataSource);
            sce.getServletContext().setAttribute("authService", authService);
            sce.getServletContext().setAttribute("jwtUtil", jwtUtil);
            sce.getServletContext().setAttribute("passwordEncoder", passwordEncoder);

            logger.info("Application initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            throw new RuntimeException("Failed to initialize application", e);
        }
    }

    private void validateConfigurations() {
        validateDatabaseConfig();
        validateJwtConfig();
    }

    private void validateDatabaseConfig() {
        String jwtSecret = System.getenv("JWT_SECRET");
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new RuntimeException("JWT_SECRET environment variable is not set");
        }
        if (jwtSecret.length() < 32) {
            throw new RuntimeException("JWT_SECRET must be at least 32 characters long");
        }
    }

    private void validateJwtConfig() {
        String jwtSecret = System.getenv("JWT_SECRET");
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            throw new RuntimeException("JWT_SECRET environment variable is not set");
        }
        if (jwtSecret.length() < 32) {
            throw new RuntimeException("JWT_SECRET must be at least 32 characters long");
        }
    }

    private DataSource createDataSource() {
        HikariConfig config = getHikariConfig();

        // MySQL specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // MySQL specific settings
        config.addDataSourceProperty("useSSL", "false");
        config.addDataSourceProperty("serverTimezone", "UTC");
        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");

        return new HikariDataSource(config);
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig config = new HikariConfig();

        // Basic configuration
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Connection pool settings
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE);
        config.setIdleTimeout(IDLE_TIMEOUT);
        config.setConnectionTimeout(CONNECTION_TIMEOUT);
        config.setValidationTimeout(VALIDATION_TIMEOUT);
        config.setPoolName("QuizAppPool");
        return config;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            DataSource dataSource = (DataSource) sce.getServletContext().getAttribute("dataSource");
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
                logger.info("Database connection pool closed successfully");
            }
        } catch (Exception e) {
            logger.error("Error closing database connection pool", e);
        }
    }
}
