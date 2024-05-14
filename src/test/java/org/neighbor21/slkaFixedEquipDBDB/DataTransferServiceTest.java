package org.neighbor21.slkaFixedEquipDBDB;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataTransferServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(DataTransferServiceTest.class);



    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void transferData() {
        int batchSize = 100;
        List<TlVdsPass> testList = new ArrayList<>();

        System.out.println("--> entity 세팅 시작");

        for (int j = 0; j < 100000; j++) {
            TlVdsPass entity = new TlVdsPass();
            TlVdsPassKey key = new TlVdsPassKey(
                    LocalDateTime.now(),
                    j + " testCm",
                    j + " testCar"
            );

            entity.setTlVdsPassKey(key);
            entity.setSpeed(j);

            testList.add(entity);
        }

        LocalDateTime before = LocalDateTime.now();

        int count = 0;
        for (TlVdsPass entity : testList) {
            entityManager.persist(entity);
            count++;
            if (count == batchSize) {
                entityManager.flush();
                System.out.println("5465465시작");

                entityManager.clear();
                count = 0;
            }
        }
        logger.info("Data to be loaded into TL_VDS_PASS table: {}", testList.size());

        // Final flush and clear outside the loop to ensure all entities are persisted
        entityManager.flush();
        entityManager.clear();
        LocalDateTime after = LocalDateTime.now();
        Duration diff = Duration.between(before, after);
        long differSec = diff.toSeconds();
        System.out.println("--> 걸린 시간 " + differSec + "초");
    }
}
