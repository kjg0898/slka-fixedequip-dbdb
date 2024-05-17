package org.neighbor21.slkaFixedEquipDBDB.entity.secondary;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TL_VDS_PASSKey;

import java.math.BigDecimal;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb
 * fileName       : TL_VDS_PASS.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : 엔티티 목록() //데이터를 적재해야 하는 db
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 */

@Entity
@Getter
@Setter
@Table(name = "TL_VDS_PASS", schema = "srlk")
@BatchSize(size = 1000)
public class TL_VDS_PASS {

    //미리 지정한 복합키 통행일시,카메라 아이디, 통행차량 아이디
    @EmbeddedId
    private TL_VDS_PASSKey tlVdsPassPK;

    // 설치위치 명
    @Column(name = "INSTLLC_NM")
    private String INSTLLC_NM;

    // 차량 분류
    @Column(name = "VHCL_CLSF")
    private String VHCL_CLSF;

    //차량 분류명
    @Column(name = "VHCL_CLSFNM")
    private String VHCL_CLSFNM;

    //차량 분류 그룹
    @Column(name = "VHCL_CLSFGRP")
    private String VHCL_CLSFGRP;

    //권역 아이디
    @Column(name = "RGSPH_ID")
    private String RGSPH_ID;

    //권역 명
    @Column(name = "RGSPH_NM")
    private String RGSPH_NM;

    //속도
    @Column(name = "SPEED")
    private BigDecimal SPEED;

    //이벤트 코드
    @Column(name = "EVNT_CD")
    private String EVNT_CD;

    //이벤트 명
    @Column(name = "EVNT_NM")
    private String EVNT_NM;
}
