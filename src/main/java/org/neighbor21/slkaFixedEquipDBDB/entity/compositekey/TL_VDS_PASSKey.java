package org.neighbor21.slkaFixedEquipDBDB.entity.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.entity
 * fileName       : CompositeKey.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : 적재할 테이블/@id 어노테이션으로는 하나의 pk 밖에 지정할수 없으므로 대신에 복합 pk 구조를 미리 정의해둚
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 */
@Getter
@Setter
@Embeddable
@EqualsAndHashCode
public class TL_VDS_PASSKey implements Serializable {

    //통행 일시
    @Column(name = "PASS_DT")
    private Timestamp PASS_DT;

    //카메라 아이디
    @Column(name = "CAMERA_ID")
    private String CAMERA_ID;

    //통행차량 아이디
    @Column(name = "PASSVHCL_ID")
    private String PASSVHCL_ID;

    // Default constructor
    public TL_VDS_PASSKey() {
    }

    // Constructor
    public TL_VDS_PASSKey(Timestamp PASS_DT, String CAMERA_ID, String PASSVHCL_ID) {
        this.PASS_DT = PASS_DT;
        this.CAMERA_ID = CAMERA_ID;
        this.PASSVHCL_ID = PASSVHCL_ID;
    }
}
