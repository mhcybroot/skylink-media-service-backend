package root.cyb.mh.skylink_media_service.infrastructure.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DatabaseMigrationRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        // Migration 1: Fix metadata_json column type
        try {
            logger.info("Executing database schema patch for photos table...");
            
            // Alter metadata_json to use TEXT type to accommodate large EXIF JSON strings
            jdbcTemplate.execute("ALTER TABLE photos ALTER COLUMN metadata_json TYPE TEXT;");
            
            logger.info("Successfully ensured metadata_json column is type TEXT.");
        } catch (Exception e) {
            // It's possible the table doesn't exist yet (e.g. fresh DB during tests), or other SQL errors, we catch and log
            logger.warn("Could not alter metadata_json column type. It may already be correct or table does not exist: {}", e.getMessage());
        }
        
        // Migration 2: Add SUPER_ADMIN to user_type check constraint
        try {
            logger.info("Updating users_user_type_check constraint to include SUPER_ADMIN...");
            
            // Drop the existing check constraint
            jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_user_type_check;");
            
            // Add the new check constraint with SUPER_ADMIN included
            jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT users_user_type_check CHECK (user_type IN ('ADMIN', 'CONTRACTOR', 'SUPER_ADMIN'));");
            
            logger.info("Successfully updated users_user_type_check constraint.");
        } catch (Exception e) {
            logger.warn("Could not update users_user_type_check constraint: {}", e.getMessage());
        }
        
        // Migration 3: Set default value for is_blocked column
        try {
            logger.info("Fixing is_blocked null values...");
            
            // Update any NULL values to false
            jdbcTemplate.execute("UPDATE users SET is_blocked = false WHERE is_blocked IS NULL;");
            
            // Alter column to have default value and be NOT NULL
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN is_blocked SET DEFAULT false;");
            jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN is_blocked SET NOT NULL;");
            
            logger.info("Successfully fixed is_blocked column.");
        } catch (Exception e) {
            logger.warn("Could not fix is_blocked column: {}", e.getMessage());
        }
    }
}
