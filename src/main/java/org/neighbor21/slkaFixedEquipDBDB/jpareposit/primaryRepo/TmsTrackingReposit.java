package org.neighbor21.slkaFixedEquipDBDB.jpareposit.primaryRepo;

import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.Tms_TrackingKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.primary.Tms_Tracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TmsTrackingReposit extends JpaRepository<Tms_Tracking, Tms_TrackingKey> {

    @Query("SELECT t FROM Tms_Tracking t WHERE t.tmsTrackingPK.timeStamp > :lastQueried")
    List<Tms_Tracking> findNewDataSince(LocalDateTime lastQueried);
}
