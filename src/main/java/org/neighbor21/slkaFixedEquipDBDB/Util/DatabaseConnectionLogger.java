package org.neighbor21.slkaFixedEquipDBDB.Util;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.Util
 * fileName       : DatabaseConnectionLogger.java
 * author         : kjg08
 * date           : 24. 4. 11.
 * description    : 디비 연결의 지속을 위해 쿼리 1을 날림
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 11.        kjg08           최초 생성
 */
@Configuration
public class DatabaseConnectionLogger {

    private final Logger logger = LoggerFactory.getLogger(DatabaseConnectionLogger.class);
    private final DataSource dataSource;

    public DatabaseConnectionLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @PostConstruct
    private void logDatabaseConnection() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            logger.info("Database connection test SUCCESSFUL");
        } catch (Exception e) {
            logger.error("Database connection test FAILED", e);
        }
    }
}
