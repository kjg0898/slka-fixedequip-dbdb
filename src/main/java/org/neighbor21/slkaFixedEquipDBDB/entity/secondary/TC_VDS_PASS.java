package org.neighbor21.slkaFixedEquipDBDB.entity.secondary;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLInsert;
import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TC_VDS_PASSKey;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb
 * fileName       : TC_VDS_PASS.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : 엔티티 목록() //데이터를 적재해야 하는 db  Hibernate Query Language (HQL): Hibernate에서 사용하는 객체 지향 쿼리 언어.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 * 2024-05-XX        kjg08           @SQLInsert 어노테이션 추가
 */

@Entity
@Getter
@Setter
@Table(name = "TC_VDS_PASS", schema = "srlk")
@BatchSize(size = 10000)
@SQLInsert(sql = "INSERT INTO srlk.TC_VDS_PASS " +
        "(CLCT_DT, EVNT_CD, EVNT_NM, INSTLLC_NM, RGSPH_ID, RGSPH_NM, SPEED, VHCL_CLSF, VHCL_CLSFGRP, VHCL_CLSFNM, CAMERA_ID, PASSVHCL_ID, PASS_DT) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
        "ON CONFLICT (PASS_DT, CAMERA_ID, PASSVHCL_ID) DO NOTHING")
public class TC_VDS_PASS {

    @EmbeddedId
    private TC_VDS_PASSKey tcVdsPassPK;

    @Column(name = "CLCT_DT")
    private Timestamp CLCT_DT;

    @Column(name = "EVNT_CD")
    private String EVNT_CD;

    @Column(name = "EVNT_NM")
    private String EVNT_NM;

    @Column(name = "INSTLLC_NM")
    private String INSTLLC_NM;

    @Column(name = "RGSPH_ID")
    private String RGSPH_ID;

    @Column(name = "RGSPH_NM")
    private String RGSPH_NM;

    @Column(name = "SPEED")
    private BigDecimal SPEED;

    @Column(name = "VHCL_CLSF")
    private String VHCL_CLSF;

    @Column(name = "VHCL_CLSFGRP")
    private String VHCL_CLSFGRP;

    @Column(name = "VHCL_CLSFNM")
    private String VHCL_CLSFNM;
}