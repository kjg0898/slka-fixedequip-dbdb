package org.neighbor21.slkaFixedEquipDBDB.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.service
 * fileName       : LastQueriedTimeService.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : 최신 데이터만 select 해오기 위해 직전에 select 한 시간대를 저장하는 서비스. 이 서비스는 최신 데이터를 조회할 때 기준이 되는 마지막 조회 시간을 관리합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 */

@Service // Spring의 서비스 컴포넌트로 등록
@Getter // Lombok을 사용하여 getter 메소드 자동 생성
public class LastQueriedTimeService {
    private LocalDateTime lastQueriedDateTime = LocalDateTime.now().minusMinutes(5); // 초기화

    /**
     * 마지막 조회 시간을 업데이트하는 메소드.
     *
     * @param dateTime 업데이트할 새로운 조회 시간
     */
    public void updateLastQueriedDateTime(LocalDateTime dateTime) {
        this.lastQueriedDateTime = dateTime;
    }
}