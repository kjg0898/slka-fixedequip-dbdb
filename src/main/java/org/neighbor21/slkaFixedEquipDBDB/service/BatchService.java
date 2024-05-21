package org.neighbor21.slkaFixedEquipDBDB.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.neighbor21.slkaFixedEquipDBDB.config.Constants;
import org.neighbor21.slkaFixedEquipDBDB.entity.secondary.TL_VDS_PASS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.function.Supplier;

@Service
public class BatchService {
    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);
    private final RetryConfig retryConfig;
    private final TransactionTemplate transactionTemplate;

    @PersistenceContext(unitName = "secondary")
    private EntityManager secondaryEntityManager;

    /**
     * BatchService 생성자.
     *
     * @param retryConfig 재시도 구성 설정
     */
    @Autowired
    public BatchService(RetryConfig retryConfig, @Qualifier("secondaryTransactionManager") PlatformTransactionManager transactionManager) {
        this.retryConfig = retryConfig;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * 엔티티 리스트를 배치로 삽입하는 메소드. Resilience4j를 사용하여 재시도 로직을 구현함.
     * 각 엔티티를 지속하고, 주기적으로 EntityManager를 플러시 및 클리어하여 메모리 사용을 최적화한다.
     * @param entities 삽입할 엔티티 리스트
     *                 //* @param existingKeys 이미 존재하는 키 세트 <<// 중복 데이터 키 조회 및 필터링 << 실 데이터에서는 중복으로 키값이 들어올 경우가 없다고 판단하여 주석처리함.
     */
    @Transactional(transactionManager = "secondaryTransactionManager")
    public void batchInsertWithRetry(List<TL_VDS_PASS> entities/*, Set<TL_VDS_PASSKey> existingKeys*/) {
        int batchSize = Constants.DEFAULT_BATCH_SIZE; // 배치 크기 설정
        Retry retry = Retry.of("batchInsert", retryConfig); // 재시도 설정

        transactionTemplate.executeWithoutResult(status -> {
            // entities 리스트를 batchSize 크기만큼씩 나누어 처리하는 루프
            for (int i = 0; i < entities.size(); i += batchSize) {
                // 현재 배치의 끝 인덱스를 계산
                int end = Math.min(i + batchSize, entities.size());
                // 현재 배치 리스트를 추출
                List<TL_VDS_PASS> batchList = entities.subList(i, end);

                // Retry.decorateSupplier를 사용하여 재시도 로직을 감싼 Supplier 생성
                Supplier<Boolean> insertSupplier = Retry.decorateSupplier(retry, () -> {
                    try {
                        for (TL_VDS_PASS entity : batchList) {
//                            // 존재하지 않는 키만 삽입
//                            if (!existingKeys.contains(entity.getTlVdsPassPK())) {
//                                secondaryEntityManager.persist(entity); // 엔티티 삽입
//                            }
                            secondaryEntityManager.persist(entity); // 엔티티 삽입
                        }
                        secondaryEntityManager.flush(); // 변경 사항을 데이터베이스에 반영
                        secondaryEntityManager.clear(); // 영속성 컨텍스트를 비움

                        return true;
                    } catch (Exception e) {
                        logger.warn("Batch insert attempt failed: {}", e.getMessage(), e);
                        throw e;
                    }
                });

                try {
                    insertSupplier.get(); // 재시도 로직 실행
                } catch (Exception e) {
                    logger.error("Failed to insert batch at index {} to {}", i, end, e);
                }
            }
        });

    }
}
