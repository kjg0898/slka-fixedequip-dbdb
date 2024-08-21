package org.neighbor21.slkaFixedEquipDBDB.handler;

import jakarta.annotation.PostConstruct;
import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.neighbor21.slkaFixedEquipDBDB.jpareposit.primaryRepo.TmsTrackingReposit;
import org.neighbor21.slkaFixedEquipDBDB.service.DataTransferService;
import org.neighbor21.slkaFixedEquipDBDB.service.LastQueriedTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.handler
 * fileName       : SaveVdsEntity.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : 5분 간격으로 새로운 데이터를 조회하고 처리하는 핸들러 클래스. 이 클래스는 정기적으로 데이터베이스에서 새로운 데이터를 조회하여 변환 및 저장 작업을 수행합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
 */

@Component // Spring의 컴포넌트 스캔 메커니즘에 의해 빈으로 등록됨을 나타냄
public class SaveVdsEntity {

    private static final Logger logger = LoggerFactory.getLogger(SaveVdsEntity.class);

    @Autowired // 필요한 의존 객체의 “타입"에 해당하는 빈을 찾아 주입
    private TmsTrackingReposit tmsTrackingRepository;

    @Autowired
    private LastQueriedTimeService lastQueriedService;

    @Autowired
    private DataTransferService dataTransferService;

    @Value("${schedule.cron}")
    private String scheduleCron;

    /**
     * 애플리케이션 실행시 프로세스 한번 실행
     */
    @PostConstruct
    public void init() {
        logger.info("Executing initial data fetch on application startup");
        fetchNewData();
    }
    /**
     * 일정한 간격으로 프로세스 실행
     */
    @Scheduled(cron = "${schedule.cron}")
    public void scheduledFetchNewData() {
        logger.info("Executing scheduled data fetch");
        fetchNewData();
    }

    /**
     * 일정한 간격 으로 새로운 데이터를 조회하여 처리하는 메소드.
     * 마지막 조회 시간 이후의 데이터를 조회하여 변환 및 저장 작업을 수행함.
     */
    public void fetchNewData() {
        long programeStartTime = System.currentTimeMillis();
        try {
            LocalDateTime lastQueried = lastQueriedService.getLastQueriedDateTime(); // 마지막 조회 시간 가져오기
            List<Tms_Tracking> newDataList = fetchDataWithRetry(lastQueried); // 마지막 조회 시간 이후의 데이터 조회
            if (!newDataList.isEmpty()) {
                logger.info("--------------------------------------------------------------------------------------------");
                logger.info("Lookup and convert data after time {} Start", lastQueried);
                // 새로운 데이터가 있을 경우, 데이터 변환 및 저장 처리
                boolean isTransferSuccessful = dataTransferService.transferData(newDataList); // DataTransferService에 새로운 데이터를 전달하여 처리
                if (isTransferSuccessful) {
                    // 데이터 전송이 성공한 경우에 가장 최신의 timestamp로 마지막 조회 시간 업데이트
                    LocalDateTime latestTimestamp = newDataList.stream()
                            .map(t -> t.getTmsTrackingPK().getTimeStamp().toLocalDateTime())
                            .max(LocalDateTime::compareTo)
                            .orElse(lastQueried);
                    lastQueriedService.updateLastQueriedDateTime(latestTimestamp);
                }
                long endTime = System.currentTimeMillis();
                logger.info("Completed Insert to TL_VDS_PASS successfully in : {} ms", endTime - programeStartTime);
                logger.info("--------------------------------------------------------------------------------------------");

            } else {
                logger.info("새로운 데이터를 찾지 못했습니다.");
            }
        } catch (DataAccessException e) {
            logger.error("데이터 액세스 중 오류 발생: ", e);
        } catch (RuntimeException e) {
            logger.error("데이터 처리 중 런타임 오류 발생: ", e);
        } catch (Exception e) {
            logger.error("데이터 처리 중 예상치 못한 오류 발생: ", e);
        }
    }
    /**
     * 마지막 조회 시간 이후의 데이터를 재시도 로직을 포함하여 가져오는 메소드.
     *
     * @param lastQueried 마지막 조회 시간
     * @return 새로운 Tms_Tracking 데이터 리스트
     */
    private List<Tms_Tracking> fetchDataWithRetry(LocalDateTime lastQueried) {
        int retryCount = 0;
        int maxRetries = 1;
        long retryDelay = 5000; // 5초

        while (retryCount <= maxRetries) {
            try {
                return tmsTrackingRepository.findNewDataSince(lastQueried);
            } catch (DataAccessException e) {
                logger.error("데이터베이스 액세스 오류, 재시도 중... (시도 횟수: {})", retryCount);
                retryCount++;
                if (retryCount > maxRetries) {
                    throw new RuntimeException("데이터 조회 실패", e);
                }
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException interruptedException) {
                    logger.error("재시도 대기 중 인터럽트 발생");
                    Thread.currentThread().interrupt();
                    return List.of();
                }
            }
        }
        return List.of();
    }
}
