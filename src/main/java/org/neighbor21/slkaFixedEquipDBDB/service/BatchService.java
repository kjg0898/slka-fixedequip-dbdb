package org.neighbor21.slkaFixedEquipDBDB.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import org.neighbor21.slkaFixedEquipDBDB.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : BatchService.java
 * author         : kjg08
 * date           : 2024-05-13
 * description    : 엔티티를 배치로 삽입하는 서비스 클래스. Resilience4j를 사용하여 재시도 로직을 구현하고, 트랜잭션 관리를 통해 데이터베이스 작업을 처리합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-05-13        kjg08           최초 생성
 */

@Service // Spring의 서비스 컴포넌트로 등록
public class BatchService {
    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);
    private final RetryConfig retryConfig;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext(unitName = "secondary") // 특정 영속성 유닛을 사용하는 EntityManager 주입
    private EntityManager secondaryEntityManager;

    @Autowired // 필요한 의존 객체를 주입받음
    public BatchService(RetryConfig retryConfig, @Qualifier("secondaryTransactionManager") PlatformTransactionManager transactionManager) {
        this.retryConfig = retryConfig;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 엔티티 리스트를 배치로 삽입하는 메소드. Resilience4j를 사용하여 재시도 로직을 구현함.
     *
     * @param entities 삽입할 엔티티 리스트
     */
    @Transactional(transactionManager = "secondaryTransactionManager") // 트랜잭션 관리 설정
    public void batchInsertWithRetry(List<?> entities) {
        int batchSize = Constants.DEFAULT_BATCH_SIZE; // 배치 크기 설정
        Retry retry = Retry.of("batchInsert", retryConfig); // 재시도 설정
        AtomicInteger index = new AtomicInteger(0); // 처리된 엔티티 수를 추적하는 AtomicInteger

        entities.forEach(entity -> {
            Supplier<Boolean> insertSupplier = Retry.decorateSupplier(retry, () -> {
                int currentIndex = index.incrementAndGet(); // 현재 인덱스 증가
                return transactionTemplate.execute(status -> {
                    try {
                        Object primaryKey = getPrimaryKey(entity); // 엔티티의 기본 키를 가져옴
                        if (primaryKey != null && (secondaryEntityManager.contains(entity) || secondaryEntityManager.find(entity.getClass(), primaryKey) != null)) {
                            secondaryEntityManager.merge(entity); // 엔티티 병합
                        } else {
                            secondaryEntityManager.persist(entity); // 엔티티 삽입
                        }
                        if (currentIndex % batchSize == 0 || currentIndex == entities.size()) {
                            secondaryEntityManager.flush(); // 변경 사항을 데이터베이스에 반영
                            secondaryEntityManager.clear(); // 영속성 컨텍스트를 비움
                        }
                        return true;
                    } catch (Exception e) {
                        logger.warn("Attempt to insert entity at index {} failed: {}", currentIndex, e.getMessage(), e);
                        status.setRollbackOnly(); // 트랜잭션 롤백
                        throw e;
                    }
                });
            });
            try {
                insertSupplier.get(); // 재시도 로직 실행
            } catch (Exception e) {
                logger.error("Failed to insert entity at index {} after retries", index.get(), e);
            }
        });

        transactionTemplate.execute(status -> {
            secondaryEntityManager.flush(); // 남은 변경 사항을 데이터베이스에 반영
            secondaryEntityManager.clear(); // 영속성 컨텍스트를 비움
            return null;
        });
    }

    /**
     * 엔티티의 기본 키를 가져오는 메소드.
     *
     * @param entity 기본 키를 가져올 엔티티
     * @return 기본 키 객체
     */
    private Object getPrimaryKey(Object entity) {
        try {
            for (Field field : entity.getClass().getDeclaredFields()) { // 엔티티의 모든 필드를 반복
                if (field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class)) { // 기본 키 필드를 찾음
                    field.setAccessible(true); // 필드 접근 가능 설정
                    return field.get(entity); // 필드 값 반환
                }
            }
        } catch (IllegalAccessException e) {
            logger.error("Failed to access primary key field: {}", e.getMessage(), e);
        }
        return null;
    }
}