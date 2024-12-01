package org.example.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;



public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    // Default values as fallback
    private static final String URL =  PropertyLoader.getProperty("db.url");
    private static final String USER = PropertyLoader.getProperty("db.username");
    private static final String PASSWORD = PropertyLoader.getProperty("db.password");

    private static final int MAX_POOL_SIZE = Integer.parseInt(PropertyLoader.getProperty("db.hikari.maximum-pool-size"));
    private static final int MIN_IDLE = Integer.parseInt(PropertyLoader.getProperty("db.hikari.minimum-idle"));
    private static final int IDLE_TIMEOUT = Integer.parseInt(PropertyLoader.getProperty("db.hikari.idle-timeout"));
    private static final int CONNECTION_TIMEOUT = Integer.parseInt(PropertyLoader.getProperty("db.hikari.connection-timeout"));
    private static final int LEAK_DETECTION_THRESHOLD = Integer.parseInt(PropertyLoader.getProperty("db.hikari.leak-detection-threshold"));


    private static final HikariDataSource dataSource;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            dataSource = initializeDataSource();
            logger.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }

    private static HikariDataSource initializeDataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Connection pool settings
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(MIN_IDLE);
        config.setIdleTimeout(IDLE_TIMEOUT);
        config.setConnectionTimeout(CONNECTION_TIMEOUT);

        if (LEAK_DETECTION_THRESHOLD > 0) {
            config.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);
        }

        return new HikariDataSource(config);
//
//        // MySQL specific settings
//        config.addDataSourceProperty("cachePrepStmts", "true");
//        config.addDataSourceProperty("prepStmtCacheSize", "250");
//        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//        config.addDataSourceProperty("useServerPrepStmts", "true");
//        config.addDataSourceProperty("useLocalSessionState", "true");
//        config.addDataSourceProperty("rewriteBatchedStatements", "true");
//        config.addDataSourceProperty("cacheResultSetMetadata", "true");
//        config.addDataSourceProperty("cacheServerConfiguration", "true");
//        config.addDataSourceProperty("elideSetAutoCommits", "true");
//        config.addDataSourceProperty("maintainTimeStats", "false");
//        config.addDataSourceProperty("useSSL", "false");
//        config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            logger.debug("Database connection acquired successfully");
            return connection;
        } catch (SQLException e) {
            logger.error("Failed to get database connection", e);
            throw e;
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isValid = conn != null && !conn.isClosed() && conn.isValid(5);
            if (isValid) {
                logger.info("Database connection test successful");
            } else {
                logger.warn("Database connection test failed");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        }
    }

    public static void closeDataSource() {
        if (dataSource != null) {
            dataSource.close();
            logger.info("Database connection pool shut down");
        }
    }

}
