package org.neighbor21.slkaFixedEquipDBDB.jpareposit.primaryRepo;

import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.Tms_TrackingKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.jpareposit.primaryRepo
 * fileName       : TmsTrackingReposit.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : Tms_Tracking 엔티티에 대한 JPA 리포지토리 인터페이스. 이 인터페이스는 데이터베이스와 상호작용하여 Tms_Tracking 엔티티의 CRUD 작업을 처리합니다. Spring Data JPA 어노테이션 JPQL 사용
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
 */

@Repository // Spring Data JPA 리포지토리로 등록
public interface TmsTrackingReposit extends JpaRepository<Tms_Tracking, Tms_TrackingKey> {

    /**
     * 마지막 조회 시간 이후의 새로운 데이터를 조회하는 메소드.
     *
     * @param lastQueried 마지막 조회 시간
     * @return 새로운 Tms_Tracking 데이터 리스트
     * jpql
     */
    @Query("SELECT t FROM Tms_Tracking t WHERE t.tmsTrackingPK.timeStamp > :lastQueried")
    List<Tms_Tracking> findNewDataSince(LocalDateTime lastQueried);
}
