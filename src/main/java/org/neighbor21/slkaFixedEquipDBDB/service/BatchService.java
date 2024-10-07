package org.neighbor21.slkaFixedEquipDBDB.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import org.neighbor21.slkaFixedEquipDBDB.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : BatchService.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : 대량의 엔티티를 배치로 삽입하는 서비스 클래스. 배치 작업 중 발생할 수 있는 예외를 처리하며 재시도를 지원합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
 * 2024-05-XX        kjg08           제네릭 메서드로 변경하여 TC_VDS_PASS 엔티티도 처리 가능하도록 수정
 */

@Service
public class BatchService {

    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext(unitName = "secondary")
    private EntityManager secondaryEntityManager;

    @Autowired
    public BatchService(@Qualifier("secondaryTransactionManager") PlatformTransactionManager transactionManager) {
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 주어진 엔티티 리스트를 배치로 삽입하는 메소드.
     *
     * @param entities 삽입할 엔티티 리스트
     * @param entityClass 엔티티 클래스
     */
    @Transactional(transactionManager = "secondaryTransactionManager")
    public <T> void batchInsertWithRetry(List<T> entities, Class<T> entityClass) {
        int batchSize = Constants.DEFAULT_BATCH_SIZE; // 배치 크기 설정

        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < entities.size(); i += batchSize) {
                int end = Math.min(i + batchSize, entities.size());
                List<T> batchList = entities.subList(i, end);

                try {
                    for (T entity : batchList) {
                        try {
                            secondaryEntityManager.persist(entity);
                        } catch (EntityExistsException e) {
                            logger.warn("중복 키 오류 발생 {}", entity);
                        } catch (PersistenceException e) {
                            logger.warn("엔티티 저장 중 오류 발생 {}", entity);
                        }
                    }
                    secondaryEntityManager.flush();
                    secondaryEntityManager.clear();
                } catch (PersistenceException e) {
                    logger.warn("배치 삽입 시도 실패, {} 엔티티, 인덱스 {}에서 {}까지: {}", entityClass.getSimpleName(), i, end, e.getMessage(), e);
                    retryBatchInsert(batchList, entityClass); // 재시도 로직 추가
                } catch (TransactionException e) {
                    logger.error("트랜잭션 오류 발생, {} 엔티티, 인덱스 {}에서 {}까지: {}", entityClass.getSimpleName(), i, end, e.getMessage(), e);
                }
            }
        });
    }

    /**
     * 실패한 배치 리스트를 재시도하는 메소드.
     *
     * @param batchList 실패한 배치 리스트
     * @param entityClass 엔티티 클래스
     */
    private <T> void retryBatchInsert(List<T> batchList, Class<T> entityClass) {
        try {
            for (T entity : batchList) {
                try {
                    secondaryEntityManager.persist(entity);
                } catch (EntityExistsException e) {
                    logger.warn("재시도 중 중복 키 오류 발생 {}", entity);
                } catch (PersistenceException e) {
                    logger.warn("재시도 중 엔티티 저장 오류 발생 {}", entity);
                }
            }
            secondaryEntityManager.flush();
            secondaryEntityManager.clear();
        } catch (PersistenceException e) {
            logger.error("재시도 중 영속성 오류 발생 ({}): {}", entityClass.getSimpleName(), e.getMessage(), e);
        } catch (TransactionException e) {
            logger.error("재시도 중 트랜잭션 오류 발생 ({}): {}", entityClass.getSimpleName(), e.getMessage(), e);
        }
    }
}