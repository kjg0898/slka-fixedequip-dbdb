package org.neighbor21.slkaFixedEquipDBDB.dto;

import jakarta.persistence.EmbeddedId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.dto
 * fileName       : TL_VDS_PASSDto.java
 * author         : kjg08
 * date           : 24. 4. 17.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 17.        kjg08           최초 생성
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TL_VDS_PASSDto {
    //미리 지정한 복합키 통행일시,카메라 아이디, 통행차량 아이디
    @EmbeddedId
    private TL_VDS_PASSKeyDto tlVdsPassPK;

    // 설치위치 명
    private String INSTLLC_NM;

    // 차량 분류
    private String VHCL_CLSF;

    //차량 분류명
    private String VHCL_CLSFNM;

    //차량 분류 그룹
    private String VHCL_CLSFGRP;

    //권역 아이디
    private String RGSPH_ID;

    //권역 명
    private String RGSPH_NM;

    //속도
    private BigDecimal SPEED;

    //이벤트 코드
    private String EVNT_CD;

    //이벤트 명
    private String EVNT_NM;
}
