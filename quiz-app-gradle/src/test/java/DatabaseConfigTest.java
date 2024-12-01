
import lombok.extern.slf4j.Slf4j;
import org.example.config.DatabaseConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;


@Slf4j
class DatabaseConfigTest {

    @BeforeAll
    static void setUp() {
        System.setProperty("DB_URL", "jdbc:mysql://localhost:3306/quiz_db?useSSL=false&allowPublicKeyRetrieval=true");
        System.setProperty("DB_USERNAME", "quizapp");
        System.setProperty("DB_PASSWORD", "quizapp123");
    }

    @Test
    void testDatabaseConnection() {
        try {
            assertTrue(DatabaseConfig.testConnection(), "Database connection should be successful");
        } catch (Exception e) {
            log.error("Database connection test failed", e);
            fail("Database connection test failed: " + e.getMessage());
        }
    }

    @Test
    void testGetConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertTrue(conn.isValid(1), "Connection should be valid");
        } catch (SQLException e) {
            log.error("Get connection test failed", e);
            fail("Get connection test failed: " + e.getMessage());
        }
    }

    @Test
    void testGetDataSource() {
        assertNotNull(DatabaseConfig.getDataSource(), "DataSource should not be null");
    }
}
