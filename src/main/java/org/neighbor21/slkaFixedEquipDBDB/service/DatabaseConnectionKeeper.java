package org.neighbor21.slkaFixedEquipDBDB.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.service
 * fileName       : DatabaseConnectionTester.java
 * author         : kjg08
 * date           : 24. 4. 12.
 * description    : db 연결 테스트 (select 1)
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 12.        kjg08           최초 생성
 */
@EnableScheduling
@Component
public class DatabaseConnectionKeeper {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionKeeper.class);

    private final DataSource secondaryDataSource;
    private final DataSource primaryDataSource;

    public DatabaseConnectionKeeper(@Qualifier("secondaryDataSource") DataSource secondaryDataSource,
                                    @Qualifier("primaryDataSource") DataSource primaryDataSource) {
        this.secondaryDataSource = secondaryDataSource;
        this.primaryDataSource = primaryDataSource;
    }

    @Scheduled(fixedDelay = 30000) // 300,000밀리초 (5분) 마다 실행
    public void keepAlivePrimaryDataSource() {
        keepAliveConnection(primaryDataSource, "VDS");
    }

    @Scheduled(fixedDelay = 30000) // 동일하게 5분마다 실행
    public void keepAliveSecondaryDataSource() {
        keepAliveConnection(secondaryDataSource, "SRLK");
    }

    private void keepAliveConnection(DataSource dataSource, String dataSourceName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            if (rs.next()) {
                int result = rs.getInt(1); // SELECT 1 쿼리의 결과를 가져옵니다.
                logger.info("{} data source keep-alive query executed successfully. Result: {}", dataSourceName, result);
            } else {
                logger.warn("{} data source keep-alive query executed but didn't return a result.", dataSourceName);
            }
        } catch (Exception e) {
            logger.error("Failed to keep alive " + dataSourceName + " data source: " + e.getMessage(), e);
        }
    }
}