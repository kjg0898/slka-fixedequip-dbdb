package org.neighbor21.slkaFixedEquipDBDB.handler;

import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.neighbor21.slkaFixedEquipDBDB.jpareposit.primaryRepo.TmsTrackingReposit;
import org.neighbor21.slkaFixedEquipDBDB.service.DataTransferService;
import org.neighbor21.slkaFixedEquipDBDB.service.LastQueriedTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    /**
     * 일정한 간격(5분)으로 새로운 데이터를 조회하여 처리하는 메소드.
     * 마지막 조회 시간 이후의 데이터를 조회하여 변환 및 저장 작업을 수행함.
     */
    @Scheduled(fixedRate = 290000) // 300000 milliseconds = 5 minutes 이지만 실제 처리 속도를 고려하여 290000 milliseconds로 설정
    public void fetchNewData() {
        long programeStartTime = System.currentTimeMillis();
        LocalDateTime lastQueried = lastQueriedService.getLastQueriedDateTime(); // 마지막 조회 시간 가져오기
        List<Tms_Tracking> newDataList = tmsTrackingRepository.findNewDataSince(lastQueried); // 마지막 조회 시간 이후의 데이터 조회

        if (!newDataList.isEmpty()) {
            logger.info("{} 시간 이후의 데이터를 조회 후 변환 처리", lastQueried);
            // 새로운 데이터가 있을 경우, 데이터 변환 및 저장 처리
            boolean isTransferSuccessful = dataTransferService.transferData(newDataList); // DataTransferService에 새로운 데이터를 전달하여 처리
            if (isTransferSuccessful) {
                // 데이터 전송이 성공한 경우에만 마지막 조회 시간 업데이트
                lastQueriedService.updateLastQueriedDateTime(LocalDateTime.now());
            }
            long endTime = System.currentTimeMillis();
            logger.info("all process complete time {} ms", endTime - programeStartTime);
        } else {
            logger.info("No new data found");
        }
    }
}
