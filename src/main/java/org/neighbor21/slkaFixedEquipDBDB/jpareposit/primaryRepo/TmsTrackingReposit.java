package org.neighbor21.slkaFixedEquipDBDB.jpareposit.primaryRepo;

import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.Tms_TrackingKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.jpareposit
 * fileName       : TmsTrackingReposit.java
 * author         : kjg08
 * date           : 2024-04-08
 * description    : JPA가 제공하는 인터페이스 중 하나로 CRUD 작업을 처리하는 메서드들을 이미 내장하고 있어 데이터 관리 작업을 좀 더 편리하게 처리할 수 있다. Tms_Tracking 테이블에 대한 select 작업을 위한 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-08        kjg08           최초 생성
 */
@Repository
public interface TmsTrackingReposit extends JpaRepository<Tms_Tracking, Tms_TrackingKey> {

    @Query("SELECT t FROM Tms_Tracking t WHERE t.tmsTrackingPK.timeStamp > :lastQueried")
    List<Tms_Tracking> findNewDataSince(LocalDateTime lastQueried);
}
