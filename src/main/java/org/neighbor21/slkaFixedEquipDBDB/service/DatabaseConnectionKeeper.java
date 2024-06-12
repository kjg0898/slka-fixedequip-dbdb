package org.neighbor21.slkaFixedEquipDBDB.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : DatabaseConnectionKeeper.java
 * author         : kjg08
 * date           : 2024-04-12
 * description    : 데이터베이스 연결 유지를 위한 클래스. 주기적으로 데이터베이스에 간단한 쿼리를 실행하여 연결을 유지합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-12        kjg08           최초 생성
 */

@EnableScheduling // Spring의 스케줄링 기능을 활성화
@Component // Spring의 컴포넌트로 등록
public class DatabaseConnectionKeeper {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionKeeper.class);

    private final DataSource secondaryDataSource;
    private final DataSource primaryDataSource;

    @Value("${schedule.keepAlivePrimaryCron}")
    private String keepAlivePrimaryCron;

    @Value("${schedule.keepAliveSecondaryCron}")
    private String keepAliveSecondaryCron;

    /**
     * 두 개의 DataSource를 주입받는 생성자.
     *
     * @param secondaryDataSource 보조 데이터 소스
     * @param primaryDataSource   기본 데이터 소스
     */
    public DatabaseConnectionKeeper(@Qualifier("secondaryDataSource") DataSource secondaryDataSource,
                                    @Qualifier("primaryDataSource") DataSource primaryDataSource) {
        this.secondaryDataSource = secondaryDataSource;
        this.primaryDataSource = primaryDataSource;
    }

    /**
     * 기본 데이터 소스의 연결을 유지하기 위한 메소드.
     * cron 표현식에 따라 실행되어 연결을 유지함.
     */
    @Scheduled(cron = "${schedule.keepAlivePrimaryCron}")
    public void keepAlivePrimaryDataSource() {
        keepAliveConnection(primaryDataSource, "Primary");
    }

    /**
     * 보조 데이터 소스의 연결을 유지하기 위한 메소드.
     * cron 표현식에 따라 실행되어 연결을 유지함.
     */
    @Scheduled(cron = "${schedule.keepAliveSecondaryCron}")
    public void keepAliveSecondaryDataSource() {
        keepAliveConnection(secondaryDataSource, "Secondary");
    }

    /**
     * 주어진 데이터 소스에 대해 연결을 유지하는 쿼리를 실행하는 메소드.
     *
     * @param dataSource     연결을 유지할 데이터 소스
     * @param dataSourceName 데이터 소스의 이름
     */
    private void keepAliveConnection(DataSource dataSource, String dataSourceName) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            if (rs.next()) {
                int result = rs.getInt(1); // SELECT 1 쿼리의 결과를 가져옴
                logger.info("{} data source keep-alive query executed successfully. Result: {}", dataSourceName, result);
            } else {
                logger.warn("{} data source keep-alive query executed but didn't return a result.", dataSourceName);
            }
        } catch (Exception e) {
            logger.error("Failed to keep alive {} data source: {}", dataSourceName, e.getMessage(), e);
        }
    }
}