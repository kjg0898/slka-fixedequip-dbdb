package org.neighbor21.slkaFixedEquipDBDB.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.neighbor21.slkaFixedEquipDBDB.Util.ParshingUtil;
import org.neighbor21.slkaFixedEquipDBDB.dto.TL_VDS_PASSDto;
import org.neighbor21.slkaFixedEquipDBDB.dto.TL_VDS_PASSKeyDto;
import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TL_VDS_PASSKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.neighbor21.slkaFixedEquipDBDB.entity.secondary.TL_VDS_PASS;
import org.neighbor21.slkaFixedEquipDBDB.jpareposit.secondaryRepo.TlVdsPassReposit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : DataTransferService.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : Primary 테이블에서 Secondary 테이블로 데이터를 옮기기 위해 타입 변환 및 파싱 후 적재하는 로직을 담당하는 서비스 클래스.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
 */
@Service // Spring의 서비스 컴포넌트로 등록
public class DataTransferService {
    private static final Logger logger = LoggerFactory.getLogger(DataTransferService.class);
    private static final Logger retryLogger = LoggerFactory.getLogger("RetryLogger");
    private final BatchService batchService;

    @PersistenceContext(unitName = "secondary") // 특정 영속성 유닛을 사용하는 EntityManager 주입
    private EntityManager secondaryEntityManager;

    @Autowired // 필요한 의존 객체를 주입받음
    public DataTransferService(BatchService batchService, TlVdsPassReposit tlVdsPassReposit) {
        this.batchService = batchService;
    }

    /**
     * Primary 데이터베이스에서 Secondary 데이터베이스로 데이터를 전송하는 메소드.
     *
     * @param newDataList 전송할 데이터 리스트
     */
    @Transactional("secondaryTransactionManager") // 트랜잭션 관리 설정
    public void transferData(List<Tms_Tracking> newDataList) {
        long startTime = System.currentTimeMillis(); // 시작 시간
        int totalRecords = newDataList.size();
        logger.info("Starting data transfer for {} records", totalRecords);
        int lastLoggedPercentage = 0;
        int batchSize = 1000;

        List<TL_VDS_PASS> batchList = new ArrayList<>();

        // 시간 측정 변수 초기화
        long totalConversionTime = 0;
        long totalBatchInsertTime = 0;
        long totalRetryTime = 0;
        long totalRecordTime = 0;
        long totalFlushTime = 0;
        long totalValidationTime = 0;

        for (int i = 0; i < newDataList.size(); i++) {
            Tms_Tracking sourceData = newDataList.get(i);
            long recordStartTime = System.currentTimeMillis();
            try {
                long validationStartTime = System.currentTimeMillis();
                if (!validateData(sourceData)) {
                    continue;
                }
                long validationEndTime = System.currentTimeMillis();
                totalValidationTime += (validationEndTime - validationStartTime);

                long conversionStartTime = System.currentTimeMillis();
                TL_VDS_PASSDto dto = convertEntityToDTO(sourceData);
                TL_VDS_PASS targetData = convertDtoToEntity(dto);
                batchList.add(targetData);
                long conversionEndTime = System.currentTimeMillis();
                totalConversionTime += (conversionEndTime - conversionStartTime);

                if (batchList.size() == batchSize) {
                    // 중복 데이터 키 조회 및 필터링 << 실 데이터에서는 중복으로 키값이 들어올 경우가 없다고 판단하여 주석처리함.
//
//                    Set<TL_VDS_PASSKey> keysToCheck = batchList.stream()
//                            .map(TL_VDS_PASS::getTlVdsPassPK)
//                            .collect(Collectors.toSet());

//                    Set<TL_VDS_PASSKey> existingKeys = tlVdsPassReposit.findExistingKeys(keysToCheck);

                    long batchInsertStartTime = System.currentTimeMillis();
                    batchService.batchInsertWithRetry(batchList/*, existingKeys*/); // 존재하는 키 전달 <<// 중복 데이터 키 조회 및 필터링 << 실 데이터에서는 중복으로 키값이 들어올 경우가 없다고 판단하여 주석처리함.
                    batchList.clear();
                    long batchInsertEndTime = System.currentTimeMillis();
                    totalBatchInsertTime += (batchInsertEndTime - batchInsertStartTime);
                }
            } catch (Exception e) {
                logger.error("Initial transfer failed for tracking PK {}, attempting retry...", sourceData.getTmsTrackingPK());
                long retryStartTime = System.currentTimeMillis();
                retryFailedData(sourceData, 0);
                long retryEndTime = System.currentTimeMillis();
                totalRetryTime += (retryEndTime - retryStartTime);
            }

            long recordEndTime = System.currentTimeMillis();
            totalRecordTime += (recordEndTime - recordStartTime);

            // 진행 상황 퍼센티지 계산 및 로그 기록
            int progressPercentage = ((i + 1) * 100) / totalRecords;
            if (progressPercentage >= lastLoggedPercentage + 10) { // 10%마다 로그 기록
                logger.info("Progress: {}%", progressPercentage);
                lastLoggedPercentage = progressPercentage;
            }
        }

        // 남은 데이터 처리
        if (!batchList.isEmpty()) {
            // 중복 데이터 키 조회 및 필터링 << 실 데이터에서는 중복으로 키값이 들어올 경우가 없다고 판단하여 주석처리함.
//            Set<TL_VDS_PASSKey> keysToCheck = batchList.stream()
//                    .map(TL_VDS_PASS::getTlVdsPassPK)
//                    .collect(Collectors.toSet());

//            Set<TL_VDS_PASSKey> existingKeys = tlVdsPassReposit.findExistingKeys(keysToCheck);

            long batchInsertStartTime = System.currentTimeMillis();
            batchService.batchInsertWithRetry(batchList/*, existingKeys*/); // 존재하는 키 전달 <<// 중복 데이터 키 조회 및 필터링 << 실 데이터에서는 중복으로 키값이 들어올 경우가 없다고 판단하여 주석처리함.
            long batchInsertEndTime = System.currentTimeMillis();
            totalBatchInsertTime += (batchInsertEndTime - batchInsertStartTime);
        }

        long flushStartTime = System.currentTimeMillis();
        secondaryEntityManager.flush();
        secondaryEntityManager.clear();
        long flushEndTime = System.currentTimeMillis();
        totalFlushTime += (flushEndTime - flushStartTime);

        long endTime = System.currentTimeMillis(); // 종료 시간
        long totalDuration = endTime - startTime; // 소요 시간 (밀리초)

        logger.info("Data transfer completed for {} records in {} ms", totalRecords, totalDuration);
        logger.info("Total validation time: {} ms", totalValidationTime);
        logger.info("Total conversion time: {} ms", totalConversionTime);
        logger.info("Total batch insert time: {} ms", totalBatchInsertTime);
        logger.info("Total retry time: {} ms", totalRetryTime);
        logger.info("Total record processing time: {} ms", totalRecordTime);
        logger.info("Total flush and clear time: {} ms", totalFlushTime);
    }

    /**
     * 주어진 데이터를 유효성 검사하는 메소드.
     *
     * @param sourceData 유효성 검사할 데이터
     * @return 유효성 검사 결과 (유효하면 true, 아니면 false)
     */
    private boolean validateData(Tms_Tracking sourceData) {
        // 예: 속도가 음수이거나 특정 범위를 초과하는지 검사
        if (sourceData.getVelocity() < 0 || sourceData.getVelocity() > 200) {
            logger.error("Invalid velocity data for tracking PK {}: {}", sourceData.getTmsTrackingPK(), sourceData.getVelocity());
            return false;
        }
        // 추가적인 유효성 검사 로직 구현
        return true;
    }

    /**
     * 데이터 전송 실패 시 재시도하는 메소드.
     *
     * @param failedData 전송에 실패한 데이터
     * @param retryCount 현재 재시도 횟수
     */
    private void retryFailedData(Tms_Tracking failedData, int retryCount) {
        if (retryCount > 3) {
            logger.error("Max retries exceeded for tracking PK {}: {}", failedData.getTmsTrackingPK(), failedData);
            return;
        }

        try {
            TL_VDS_PASSDto dto = convertEntityToDTO(failedData);
            TL_VDS_PASS retryData = convertDtoToEntity(dto);
            batchService.batchInsertWithRetry(List.of(retryData)/*, Set.of()*/); // 재시도 시 기존 키가 없는 상태로 처리 <<// 중복 데이터 키 조회 및 필터링 << 실 데이터에서는 중복으로 키값이 들어올 경우가 없다고 판단하여 주석처리함.
            retryLogger.info("Retry successful for tracking PK {}", failedData.getTmsTrackingPK());
        } catch (Exception retryException) {
            retryLogger.error("Retry failed for tracking PK {}: {}, Attempt: {}", failedData.getTmsTrackingPK(), failedData, retryCount, retryException);
            retryFailedData(failedData, retryCount + 1);
        }
    }

    /**
     * Tms_Tracking 엔티티를 TL_VDS_PASSDto로 변환하는 메소드.
     *
     * @param entity 변환할 엔티티
     * @return 변환된 DTO 객체
     */
    private TL_VDS_PASSDto convertEntityToDTO(Tms_Tracking entity) {
        TL_VDS_PASSKeyDto keyDto = new TL_VDS_PASSKeyDto();
        TL_VDS_PASSDto dto = new TL_VDS_PASSDto();
        // 통행일시
        keyDto.setPASS_DT(java.sql.Timestamp.valueOf(entity.getTmsTrackingPK().getTimeStamp().toLocalDateTime()));
        // 카메라 아이디
        keyDto.setCAMERA_ID(String.valueOf(entity.getTmsTrackingPK().getCamID()));
        // 통행차량 아이디
        keyDto.setPASSVHCL_ID(entity.getTmsTrackingPK().getTrackingID());

        // TL_VDS_PASSKey
        dto.setTlVdsPassPK(keyDto);
        // 설치위치 명
        dto.setINSTLLC_NM(entity.getSiteName());
        // 차량 분류 (primary db 에서는 해당 값이 차량코드로 들어옴. 이것을 secondary 디비에서는 차량분류항목에 따라 파싱하여 적재할것임)
        dto.setVHCL_CLSF(ParshingUtil.getVehicleClassification(entity.getLabelID()));
        // 차량 분류명
        dto.setVHCL_CLSFNM(entity.getLabelName());
        // 차량 분류 그룹
        dto.setVHCL_CLSFGRP(String.valueOf(entity.getLabelGroup()));
        // 권역 아이디
        dto.setRGSPH_ID(String.valueOf(entity.getRegionID()));
        // 권역 명
        dto.setRGSPH_NM(entity.getRegionName());
        // 속도
        dto.setSPEED(BigDecimal.valueOf(entity.getVelocity()));
        // 이벤트 코드
        dto.setEVNT_CD(String.valueOf(entity.getEventID()));
        // 이벤트 명
        dto.setEVNT_NM(entity.getEventName());

        return dto;
    }

    /**
     * TL_VDS_PASSDto를 TL_VDS_PASS 엔티티로 변환하는 메소드.
     *
     * @param dto 변환할 DTO
     * @return 변환된 엔티티 객체
     */
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
