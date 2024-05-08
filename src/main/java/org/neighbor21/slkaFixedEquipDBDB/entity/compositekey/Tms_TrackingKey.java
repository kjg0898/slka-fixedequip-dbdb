package org.neighbor21.slkaFixedEquipDBDB.entity.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.entity.compositekey
 * fileName       : Tms_TrackingKey.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : @id 어노테이션으로는 하나의 pk 밖에 지정할수 없으므로 대신에 복합 pk 구조를 미리 정의해둚
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 */
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class Tms_TrackingKey implements Serializable {

    //통행 일시
    @Column(name = "SiteName")
    private Timestamp timeStamp;
    //카메라 고유 아이디
    @Column(name = "SiteName")
    private int camID;
    //통행 차량 고유 번호(번호판X)
    @Column(name = "SiteName")
    private String trackingID;

    // Default constructor
    public Tms_TrackingKey() {
    }

    // Constructor
    public Tms_TrackingKey(Timestamp timeStamp, int camID, String trackingID) {
        this.timeStamp = timeStamp;
        this.camID = camID;
        this.trackingID = trackingID;
    }
}
