package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("Executing database schema patch for photos table...");
            
            // Alter metadata_json to use TEXT type to accommodate large EXIF JSON strings
            jdbcTemplate.execute("ALTER TABLE photos ALTER COLUMN metadata_json TYPE TEXT;");
            
            logger.info("Successfully ensured metadata_json column is type TEXT.");
        } catch (Exception e) {
            // It's possible the table doesn't exist yet (e.g. fresh DB during tests), or other SQL errors, we catch and log
            logger.warn("Could not alter metadata_json column type. It may already be correct or table does not exist: {}", e.getMessage());
        }
    }
}
