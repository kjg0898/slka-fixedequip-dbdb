package org.neighbor21.slkaFixedEquipDBDB.handler;

import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.neighbor21.slkaFixedEquipDBDB.jpareposit.primary.TmsTrackingReposit;
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
 * packageName    : org.neighbor21.slkafixedequipdbdb.handler
 * fileName       : DataFetchHendler.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : 5분간격으로 데이터 조회 후 파싱 처리
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
 */
@Component
public class SaveVdsEntity {
    private static final Logger logger = LoggerFactory.getLogger(SaveVdsEntity.class);


    @Autowired//필요한 의존 객체의 “타입"에 해당하는 빈을 찾아 주입한다.
    private TmsTrackingReposit tmsTrackingRepository;

    @Autowired
    private LastQueriedTimeService lastQueriedService;

    @Autowired
    private DataTransferService dataTransferService;

    //@EnableScheduling 어노테이션은 스프링의 스케줄링 기능을 활성화하는 데 사용됩니다. 이 어노테이션이 메인 클래스에 추가되면, 스프링 부트는 애플리케이션 내에서 @Scheduled 어노테이션이 붙은 메소드를 찾아 해당 메소드를 정의된 스케줄에 따라 명시된 시간을 주기로 자동으로 실행합니다
    @Scheduled(fixedRate = 295000) // 300000 milliseconds = 5 minutes 이지만 실제 처리되는 속도 고려하여 295000 초로 설정
    public void fetchNewData() {
        LocalDateTime lastQueried = lastQueriedService.getLastQueriedDateTime();
        List<Tms_Tracking> newDataList = tmsTrackingRepository.findNewDataSince(lastQueried);
        if (!newDataList.isEmpty()) {
            logger.info("{} 시간 이후의 데이터를 조회 후 변환 처리", lastQueried);
            logger.info("조회한 데이터 내용과 크기 : {},{}",newDataList, newDataList.size());
            // 새로운 데이터가 있을 경우, 데이터 변환 및 저장 처리
           dataTransferService.transferData(newDataList); // DataTransferService에 새로운 데이터를 전달하여 처리하도록 수정
        } else {
            logger.info("No new data found");
        }
        // 마지막 조회 시간 업데이트
        lastQueriedService.updateLastQueriedDateTime(LocalDateTime.now());
    }
}
