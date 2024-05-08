package org.neighbor21.slkaFixedEquipDBDB.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.dto
 * fileName       : TL_VDS_PASSKeyDto.java
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
public class TL_VDS_PASSKeyDto {

    private Timestamp PASS_DT;
    private String CAMERA_ID;
    private String PASSVHCL_ID;
}
