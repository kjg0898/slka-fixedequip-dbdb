package org.neighbor21.slkaFixedEquipDBDB.service;

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

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : BatchService.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : 대량의 TL_VDS_PASS 엔티티를 배치로 삽입하는 서비스 클래스. 배치 작업 중 발생할 수 있는 예외를 처리하며 재시도를 지원합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
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
     * 주어진 TL_VDS_PASS 엔티티 리스트를 배치로 삽입하는 메소드.
     *
     * @param entities 삽입할 엔티티 리스트
     */
    @Transactional(transactionManager = "secondaryTransactionManager")
    public void batchInsertWithRetry(List<TL_VDS_PASS> entities) {
        int batchSize = Constants.DEFAULT_BATCH_SIZE; // 배치 크기 설정

        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < entities.size(); i += batchSize) {
                int end = Math.min(i + batchSize, entities.size());
                List<TL_VDS_PASS> batchList = entities.subList(i, end);

                try {
                    for (TL_VDS_PASS entity : batchList) {
                        try {
                            secondaryEntityManager.persist(entity);
                        } catch (Exception e) {
                            logger.warn("중복 키 오류 발생, 엔티티 키 {}: {}", entity.getTlVdsPassPK(), e.getMessage());
                        }
                    }
                    secondaryEntityManager.flush();
                    secondaryEntityManager.clear();
                } catch (Exception e) {
                    logger.warn("배치 삽입 시도 실패, 인덱스 {}에서 {}까지: {}", i, end, e.getMessage(), e);
                    throw e; // 예외를 다시 던져 트랜잭션을 롤백합니다.
                }
            }
        });
    }
}
