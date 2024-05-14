package org.neighbor21.slkaFixedEquipDBDB.service;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.neighbor21.slkaFixedEquipDBDB.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : BatchService.java
 * author         : kjg08
 * date           : 24. 5. 13.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 5. 13.        kjg08           최초 생성
 */
@Service
public class BatchService {
    private static final Logger logger = LoggerFactory.getLogger(BatchService.class);
    private final RetryConfig retryConfig;

    @PersistenceContext
    private EntityManager entityManager;


    @Autowired
    public BatchService(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }

    @Transactional
    public void batchInsertWithRetry(List<?> entities) {
        int batchSize = Constants.DEFAULT_BATCH_SIZE;
        Retry retry = Retry.of("batchInsert", retryConfig);
        AtomicInteger index = new AtomicInteger(0);

        entities.forEach(entity -> {
            Supplier<Boolean> insertSupplier = Retry.decorateSupplier(retry, () -> {
                int currentIndex = index.incrementAndGet();
                try {
                    entityManager.persist(entity);
                    if (currentIndex % batchSize == 0 || currentIndex == entities.size()) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                    return true;
                } catch (Exception e) {
                    logger.warn("Attempt to insert entity at index {} failed: {}", currentIndex, e.getMessage(), e);
                    entityManager.clear();
                    throw e;
                }
            });
            try {
                insertSupplier.get();
            } catch (Exception e) {
                logger.error("Failed to insert entity at index {} after retries", index.get(), e);
            }
        });
        entityManager.flush();
        entityManager.clear();
    }

//    private void backOff(int retryCount) {
//        try {
//            long backOffTime = (long) Math.pow(2, retryCount) * Constants.BASE_BACKOFF_TIME_MS;
//            Thread.sleep(backOffTime);
//        } catch (InterruptedException ie) {
//            Thread.currentThread().interrupt();
//            logger.error("Thread interrupted during backoff delay", ie);
//        }
//    }
}