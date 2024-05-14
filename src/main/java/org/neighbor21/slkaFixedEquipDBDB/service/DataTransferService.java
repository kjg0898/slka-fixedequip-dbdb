package org.neighbor21.slkaFixedEquipDBDB.service;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.service
 * fileName       : DataTransferService.java
 * author         : kjg08
 * date           : 24. 4. 8.
 * description    : primary 테이블에서 secondary 테이블로 옮기기 위해 타입 변환 및 파싱 후 적재하는 로직
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 8.        kjg08           최초 생성
 */

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.neighbor21.slkaFixedEquipDBDB.Util.ParshingUtil;
import org.neighbor21.slkaFixedEquipDBDB.dto.TL_VDS_PASSDto;
import org.neighbor21.slkaFixedEquipDBDB.dto.TL_VDS_PASSKeyDto;
import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TL_VDS_PASSKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.neighbor21.slkaFixedEquipDBDB.entity.secondary.TL_VDS_PASS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DataTransferService {

    private static final Logger logger = LoggerFactory.getLogger(DataTransferService.class);
    private static final Logger retryLogger = LoggerFactory.getLogger("RetryLogger");

    private final BatchService batchService;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DataTransferService( BatchService batchService) {
        this.batchService = batchService;
    }

    @Transactional //메서드가 트랜잭션이 되도록 보장 (트랜잭션 내 연산은 모두 독립적으로 이루어지며 오류가 났을때 해당 트랙잭션은 취소되어 원래대로 돌아간다)
    public void transferData(List<Tms_Tracking> newDataList) {
        int batchSize = 100;
        for (int i = 0; i < newDataList.size(); i++) {
            Tms_Tracking sourceData = newDataList.get(i);
            try {
                if (!validateData(sourceData)) {
                    continue;  // Skip processing if data validation fails
                }
                TL_VDS_PASSDto dto = convertEntityToDTO(sourceData);
                TL_VDS_PASS targetData = convertDtoToEntity(dto);
                logger.info("Data to be loaded into TL_VDS_PASS table: {}", targetData);
                //entityManager.persist(targetData);
                batchService.batchInsertWithRetry(List.of(targetData)); // Using BatchService for insertion

                if (i % batchSize == 0 && i > 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            } catch (Exception e) {
                logger.error("Initial transfer failed for tracking PK {}, attempting retry...", sourceData.getTmsTrackingPK());
                retryFailedData(sourceData, 0);
            }
        }
        entityManager.flush();
        entityManager.clear();
    }

    private boolean validateData(Tms_Tracking sourceData) {
        // 예: 속도가 음수이거나 특정 범위를 초과하는지 검사
        if (sourceData.getVelocity() < 0 || sourceData.getVelocity() > 200) {
            logger.error("Invalid velocity data for tracking PK {}: {}", sourceData.getTmsTrackingPK(), sourceData.getVelocity());
            return false;
        }
        // 추가적인 유효성 검사 로직 구현
        return true;
    }


    private void retryFailedData(Tms_Tracking failedData, int retryCount) {
        if (retryCount > 3) { // Maximum retries limit set to 3
            logger.error("Max retries exceeded for tracking PK {}: {}", failedData.getTmsTrackingPK(), failedData);
            return; // Stop retrying after 3 attempts
        }

        try {
            // Retry logic, attempt to convert and persist the data again
            TL_VDS_PASSDto dto = convertEntityToDTO(failedData);
            TL_VDS_PASS retryData = convertDtoToEntity(dto);
            batchService.batchInsertWithRetry(List.of(retryData)); // Using BatchService for retry
           // entityManager.persist(retryData);
           // entityManager.flush();
           // entityManager.clear();
            retryLogger.info("Retry successful for tracking PK {}", failedData.getTmsTrackingPK());
        } catch (Exception retryException) {
            retryLogger.error("Retry failed for tracking PK {}: {}, Attempt: {}", failedData.getTmsTrackingPK(), failedData, retryCount, retryException);
            retryFailedData(failedData, retryCount + 1); // Recursive call to retry, incrementing the retry count
        }
    }

    private TL_VDS_PASSDto convertEntityToDTO(Tms_Tracking entity) {


        TL_VDS_PASSKeyDto keyDto = new TL_VDS_PASSKeyDto();
        TL_VDS_PASSDto dto = new TL_VDS_PASSDto();
        //통행일시
        keyDto.setPASS_DT(java.sql.Timestamp.valueOf(entity.getTmsTrackingPK().getTimeStamp().toLocalDateTime()));
        //카메라 아이디
        keyDto.setCAMERA_ID(String.valueOf(entity.getTmsTrackingPK().getCamID())); // getCameraId() 대신 getCamID() 사용
        //통행차량 아이디
        keyDto.setPASSVHCL_ID(entity.getTmsTrackingPK().getTrackingID()); // getTrackingId() 대신 getTrackingID() 사용

        // TL_VDS_PASSKey
        dto.setTlVdsPassPK(keyDto);
        //설치위치 명
        dto.setINSTLLC_NM(entity.getSiteName());
        //차량 분류(primart db 에서는 해당 값이 차량코드로 들어옴. 이것을 secondary 디비에서는 차량분류항목에 따라 파싱하여 적재할것임.
        //0:승용차/SUV, 1:소형버스, 2:대형버스 3:트럭, 4:대형 트레일러, 5:오토바이/자전거, 6:보행자 8:VAN/승합차/스타렉스, 9:삼륜차
        dto.setVHCL_CLSF(ParshingUtil.getVehicleClassification(entity.getLabelID()));
        //차량 분류명
        dto.setVHCL_CLSFNM(entity.getLabelName());
        //차량 분류 그룹
        dto.setVHCL_CLSFGRP(String.valueOf(entity.getLabelGroup()));
        //권역 아이디
        dto.setRGSPH_ID(String.valueOf(entity.getRegionID()));
        //권역 명
        dto.setRGSPH_NM(entity.getRegionName());
        //속도
        dto.setSPEED(BigDecimal.valueOf(entity.getVelocity()));
        //이벤트 코드
        dto.setEVNT_CD(String.valueOf(entity.getEventID()));
        //이벤트 명
        dto.setEVNT_NM(entity.getEventName());

        return dto;
    }

    private TL_VDS_PASS convertDtoToEntity(TL_VDS_PASSDto dto) {
        TL_VDS_PASS entity = new TL_VDS_PASS();
        TL_VDS_PASSKey key = new TL_VDS_PASSKey();

        // Key 설정
        key.setPASS_DT(dto.getTlVdsPassPK().getPASS_DT());
        key.setCAMERA_ID(dto.getTlVdsPassPK().getCAMERA_ID());
        key.setPASSVHCL_ID(dto.getTlVdsPassPK().getPASSVHCL_ID());
        entity.setTlVdsPassPK(key);

        // 다른 필드 설정
        entity.setINSTLLC_NM(dto.getINSTLLC_NM());
        entity.setVHCL_CLSF(dto.getVHCL_CLSF());
        entity.setVHCL_CLSFNM(dto.getVHCL_CLSFNM());
        entity.setVHCL_CLSFGRP(dto.getVHCL_CLSFGRP());
        entity.setRGSPH_ID(dto.getRGSPH_ID());
        entity.setRGSPH_NM(dto.getRGSPH_NM());
        entity.setSPEED(dto.getSPEED());
        entity.setEVNT_CD(dto.getEVNT_CD());
        entity.setEVNT_NM(dto.getEVNT_NM());

        return entity;
    }

}