package org.neighbor21.slkaFixedEquipDBDB.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.neighbor21.slkaFixedEquipDBDB.Util.ParshingUtil;
import org.neighbor21.slkaFixedEquipDBDB.config.Constants;
import org.neighbor21.slkaFixedEquipDBDB.dto.TL_VDS_PASSDto;
import org.neighbor21.slkaFixedEquipDBDB.dto.TL_VDS_PASSKeyDto;
import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TL_VDS_PASSKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TC_VDS_PASSKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.neighbor21.slkaFixedEquipDBDB.entity.secondary.TL_VDS_PASS;
import org.neighbor21.slkaFixedEquipDBDB.entity.secondary.TC_VDS_PASS;
import org.neighbor21.slkaFixedEquipDBDB.jpareposit.secondaryRepo.TlVdsPassReposit;
import org.neighbor21.slkaFixedEquipDBDB.jpareposit.secondaryRepo.TcVdsPassReposit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : DataTransferService.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : Primary 테이블에서 Secondary 테이블(TL_VDS_PASS 및 TC_VDS_PASS)로 데이터를 옮기기 위해 타입 변환 및 파싱 후 적재하는 로직을 담당하는 서비스 클래스.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
 * 2024-05-XX        kjg08           TC_VDS_PASS 테이블 처리 로직 추가
 */
@Service
public class DataTransferService {
    private static final Logger logger = LoggerFactory.getLogger(DataTransferService.class);
    private static final Logger retryLogger = LoggerFactory.getLogger("RetryLogger");
    private final BatchService batchService;
    private final TcVdsPassReposit tcVdsPassReposit;

    @PersistenceContext(unitName = "secondary") // 특정 영속성 유닛을 사용하는 EntityManager 주입
    private EntityManager secondaryEntityManager;

    @Autowired // 필요한 의존 객체를 주입받음
    public DataTransferService(BatchService batchService, TlVdsPassReposit tlVdsPassReposit, TcVdsPassReposit tcVdsPassReposit) {
        this.batchService = batchService;
        this.tcVdsPassReposit = tcVdsPassReposit;
    }

    /**
     * 데이터를 Primary 테이블에서 Secondary 테이블(TL_VDS_PASS 및 TC_VDS_PASS)로 전송
     *
     * @param newDataList Primary 테이블에서 가져온 데이터 리스트
     * @return 전송 성공 여부
     */
    @Transactional("secondaryTransactionManager") // 트랜잭션 관리 설정
    public boolean transferData(List<Tms_Tracking> newDataList) {
        long startTime = System.currentTimeMillis(); // 시작 시간
        int totalRecords = newDataList.size();
        logger.info("Starting data transfer for {} records", totalRecords);
        int lastLoggedPercentage = 0;
        int batchSize = Constants.DEFAULT_BATCH_SIZE; // 배치 크기 설정;

        List<TL_VDS_PASS> tlBatchList = new ArrayList<>();
        List<TC_VDS_PASS> tcBatchList = new ArrayList<>();
        boolean transferSuccessful = true; // 데이터 전송 성공 여부

        // TC_VDS_PASS 테이블에서 24시간 이전의 데이터 삭제 (별도 트랜잭션)
        deleteOldData();

        // 시간 측정 변수 초기화
        long totalConversionTime = 0;
        long totalBatchInsertTime = 0;
        long totalRetryTime = 0;
        long totalRecordTime = 0;

        for (int i = 0; i < newDataList.size(); i++) {
            Tms_Tracking sourceData = newDataList.get(i);
            long recordStartTime = System.currentTimeMillis();
            try {
                // 데이터 유효성 검사
                if (!validateData(sourceData)) {
                    continue;
                }

                // 엔티티를 DTO로 변환
                long conversionStartTime = System.currentTimeMillis();
                TL_VDS_PASSDto dto = convertEntityToDTO(sourceData);
                TL_VDS_PASS tlTargetData = convertDtoToTlEntity(dto);
                TC_VDS_PASS tcTargetData = convertDtoToTcEntity(dto);
                tlBatchList.add(tlTargetData);
                tcBatchList.add(tcTargetData);
                long conversionEndTime = System.currentTimeMillis();
                totalConversionTime += (conversionEndTime - conversionStartTime);

                // 배치 크기에 도달하면 배치 삽입 수행
                if (tlBatchList.size() == batchSize) {
                    long batchInsertStartTime = System.currentTimeMillis();
                    batchService.batchInsertWithRetry(tlBatchList, TL_VDS_PASS.class);
                    batchService.batchInsertWithRetry(tcBatchList, TC_VDS_PASS.class);
                    tlBatchList.clear();
                    tcBatchList.clear();
                    long batchInsertEndTime = System.currentTimeMillis();
                    totalBatchInsertTime += (batchInsertEndTime - batchInsertStartTime);
                }
            } catch (DataAccessException e) {
                logger.error("데이터 액세스 오류 발생. tracking PK {}", sourceData.getTmsTrackingPK());
                long retryStartTime = System.currentTimeMillis();
                boolean retrySuccess = retryFailedData(sourceData, 0);
                long retryEndTime = System.currentTimeMillis();
                totalRetryTime += (retryEndTime - retryStartTime);
                if (!retrySuccess) {
                    transferSuccessful = false;
                }
            } catch (IllegalArgumentException e) {
                logger.error("잘못된 인자 오류 발생. tracking PK {}", sourceData.getTmsTrackingPK());
                transferSuccessful = false;
            } catch (RuntimeException e) {
                logger.error("런타임 오류 발생. tracking PK {}", sourceData.getTmsTrackingPK());
                long retryStartTime = System.currentTimeMillis();
                boolean retrySuccess = retryFailedData(sourceData, 0);
                long retryEndTime = System.currentTimeMillis();
                totalRetryTime += (retryEndTime - retryStartTime);
                if (!retrySuccess) {
                    transferSuccessful = false;
                }
            } catch (Exception e) {
                logger.error("예상치 못한 오류 발생. tracking PK {}", sourceData.getTmsTrackingPK());
                transferSuccessful = false;
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
        if (!tlBatchList.isEmpty()) {
            long batchInsertStartTime = System.currentTimeMillis();
            batchService.batchInsertWithRetry(tlBatchList, TL_VDS_PASS.class);
            batchService.batchInsertWithRetry(tcBatchList, TC_VDS_PASS.class);
            long batchInsertEndTime = System.currentTimeMillis();
            totalBatchInsertTime += (batchInsertEndTime - batchInsertStartTime);
        }

        // EntityManager 플러시 및 클리어
        secondaryEntityManager.flush();
        secondaryEntityManager.clear();

        long endTime = System.currentTimeMillis(); // 종료 시간
        long totalDuration = endTime - startTime; // 소요 시간 (밀리초)

        logger.info("Data transfer completed for {} records in {} ms", totalRecords, totalDuration);
        logger.info("Total conversion time: {} ms, Total batch insert time: {} ms, Total retry time: {} ms", totalConversionTime, totalBatchInsertTime, totalRetryTime);
        logger.info("Total record processing time: {} ms", totalRecordTime);

        return transferSuccessful; // 데이터 전송 성공 여부 반환
    }

    protected void deleteOldData() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minus(24, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
        int deletedCount = tcVdsPassReposit.deleteDataOlderThanOneDay(oneDayAgo);
        logger.info("Deleted {} records older than 24 hours from TC_VDS_PASS table", deletedCount);
        // 이 메서드가 종료되면 트랜잭션이 커밋됩니다.
    }


    /**
     * 데이터를 유효성 검사
     *
     * @param sourceData 검증할 데이터
     * @return 유효성 검사 결과
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
     * 재시도 로직을 수행
     *
     * @param failedData 재시도할 데이터
     * @param retryCount 재시도 횟수
     * @return 재시도 성공 여부
     */
    private boolean retryFailedData(Tms_Tracking failedData, int retryCount) {
        if (retryCount > 1) {
            logger.error("Max retries exceeded for tracking PK {}: {}", failedData.getTmsTrackingPK(), failedData);
            return false;
        }

        try {
            TL_VDS_PASSDto dto = convertEntityToDTO(failedData);
            TL_VDS_PASS tlData = convertDtoToTlEntity(dto);
            TC_VDS_PASS tcData = convertDtoToTcEntity(dto);
            batchService.batchInsertWithRetry(List.of(tlData), TL_VDS_PASS.class);
            batchService.batchInsertWithRetry(List.of(tcData), TC_VDS_PASS.class);
            retryLogger.info("Retry successful for tracking PK {}", failedData.getTmsTrackingPK());
            return true;
        } catch (DataIntegrityViolationException e) {
            logger.error("Retry {} failed for tracking PK {} due to constraint violation, skipping record.", retryCount, failedData.getTmsTrackingPK());
            return false;
        } catch (JpaSystemException e) {
            retryLogger.error("Retry {} failed for tracking PK {}, attempting retry again...", retryCount, failedData.getTmsTrackingPK());
            return retryFailedData(failedData, retryCount + 1);
        } catch (Exception e) {
            retryLogger.error("Retry {} failed for tracking PK {}, attempting retry again... Error: ", retryCount, failedData.getTmsTrackingPK(), e);
            return retryFailedData(failedData, retryCount + 1);
        }
    }

    /**
     * 엔티티를 DTO로 변환
     *
     * @param entity 변환할 엔티티
     * @return 변환된 DTO
     */
    private TL_VDS_PASSDto convertEntityToDTO(Tms_Tracking entity) {
        TL_VDS_PASSKeyDto keyDto = new TL_VDS_PASSKeyDto();
        TL_VDS_PASSDto dto = new TL_VDS_PASSDto();
        keyDto.setPASS_DT(java.sql.Timestamp.valueOf(entity.getTmsTrackingPK().getTimeStamp().toLocalDateTime()));
        keyDto.setCAMERA_ID(String.valueOf(entity.getTmsTrackingPK().getCamID()));
        keyDto.setPASSVHCL_ID(entity.getTmsTrackingPK().getTrackingID());
        dto.setTlVdsPassPK(keyDto);
        dto.setINSTLLC_NM(entity.getSiteName());
        dto.setVHCL_CLSF(String.valueOf(entity.getLabelID()));
        dto.setVHCL_CLSFNM(entity.getLabelName());
        dto.setVHCL_CLSFGRP(String.valueOf(entity.getLabelGroup()));
        dto.setRGSPH_ID(String.valueOf(entity.getRegionID()));
        dto.setRGSPH_NM(entity.getRegionName());
        dto.setSPEED(BigDecimal.valueOf(entity.getVelocity()));
        dto.setEVNT_CD(String.valueOf(entity.getEventID()));
        dto.setEVNT_NM(entity.getEventName());
        return dto;
    }

    /**
     * DTO를 TL_VDS_PASS 엔티티로 변환
     *
     * @param dto 변환할 DTO
     * @return 변환된 TL_VDS_PASS 엔티티
     */
    private TL_VDS_PASS convertDtoToTlEntity(TL_VDS_PASSDto dto) {
        TL_VDS_PASS entity = new TL_VDS_PASS();
        TL_VDS_PASSKey key = new TL_VDS_PASSKey();
        key.setPASS_DT(dto.getTlVdsPassPK().getPASS_DT());
        key.setCAMERA_ID(dto.getTlVdsPassPK().getCAMERA_ID());
        key.setPASSVHCL_ID(dto.getTlVdsPassPK().getPASSVHCL_ID());
        entity.setTlVdsPassPK(key);
        entity.setINSTLLC_NM(dto.getINSTLLC_NM());
        entity.setVHCL_CLSF(dto.getVHCL_CLSF());
        entity.setVHCL_CLSFNM(dto.getVHCL_CLSFNM());
        entity.setVHCL_CLSFGRP(dto.getVHCL_CLSFGRP());
        entity.setRGSPH_ID(dto.getRGSPH_ID());
        entity.setRGSPH_NM(dto.getRGSPH_NM());
        entity.setSPEED(dto.getSPEED());
        entity.setEVNT_CD(dto.getEVNT_CD());
        entity.setEVNT_NM(dto.getEVNT_NM());
        entity.setCLCT_DT(Timestamp.valueOf(LocalDateTime.now())); // 수집일시로 현재 시간을 설정
        return entity;
    }

    /**
     * DTO를 TC_VDS_PASS 엔티티로 변환
     *
     * @param dto 변환할 DTO
     * @return 변환된 TC_VDS_PASS 엔티티
     */
    private TC_VDS_PASS convertDtoToTcEntity(TL_VDS_PASSDto dto) {
        TC_VDS_PASS entity = new TC_VDS_PASS();
        TC_VDS_PASSKey key = new TC_VDS_PASSKey();
        key.setPASS_DT(dto.getTlVdsPassPK().getPASS_DT());
        key.setCAMERA_ID(dto.getTlVdsPassPK().getCAMERA_ID());
        key.setPASSVHCL_ID(dto.getTlVdsPassPK().getPASSVHCL_ID());
        entity.setTcVdsPassPK(key);
        entity.setINSTLLC_NM(dto.getINSTLLC_NM());
        entity.setVHCL_CLSF(dto.getVHCL_CLSF());
        entity.setVHCL_CLSFNM(dto.getVHCL_CLSFNM());
        entity.setVHCL_CLSFGRP(dto.getVHCL_CLSFGRP());
        entity.setRGSPH_ID(dto.getRGSPH_ID());
        entity.setRGSPH_NM(dto.getRGSPH_NM());
        entity.setSPEED(dto.getSPEED());
        entity.setEVNT_CD(dto.getEVNT_CD());
        entity.setEVNT_NM(dto.getEVNT_NM());
        entity.setCLCT_DT(Timestamp.valueOf(LocalDateTime.now())); // 수집일시로 현재 시간을 설정
        return entity;
    }
}