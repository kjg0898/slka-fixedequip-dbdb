package org.neighbor21.slkaFixedEquipDBDB.entity.primary;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.Tms_TrackingKey;


/**
 * packageName    : org.neighbor21.slkafixedequipdbdb
 * fileName       : Tms_Tracking.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : 엔티티 목록() //데이터를 가져와야 하는 db
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 */

@Entity
@Getter
@Setter
@Table(name = "\"Tms_Tracking\"", schema = "public")
public class Tms_Tracking {

    //미리 지정한 복합키 통행일시,카메라 고유 아이디, 통행차량 고유번호(번호판x)
    @EmbeddedId
    private Tms_TrackingKey tmsTrackingPK;

    // 설치위치 이름
    @Column(name = "\"SiteName\"")
    private String SiteName;

    //차량 분류 코드
    @Column(name = "\"LabelID\"")
    private int LabelID;

    //차량 분류 이름
    @Column(name = "\"LabelName\"")
    private String LabelName;

    //차량 분류 그룹
    @Column(name = "\"LabelGroup\"")
    private int LabelGroup;

    //지역 아이디
    @Column(name = "\"RegionID\"")
    private int RegionID;

    //지역 이름
    @Column(name = "\"RegionName\"")
    private String RegionName;

    //속도
    @Column(name = "\"Velocity\"")
    private int Velocity;

    //이벤트
    @Column(name = "\"EventID\"")
    private Integer EventID;

    //이벤트 설명
    @Column(name = "\"EventName\"")
    private String EventName;
}
