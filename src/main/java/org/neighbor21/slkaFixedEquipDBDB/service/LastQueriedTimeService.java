package org.neighbor21.slkaFixedEquipDBDB.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.service
 * fileName       : LastQueriedTime.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : 최신 데이터만 select 해오기 위해 직전에 select 한 시간대 저장 하는 서비스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 */

@Service
public class LastQueriedTimeService { private LocalDateTime lastQueriedDateTime = LocalDateTime.now().minusMinutes(5); // 초기화

    public LocalDateTime getLastQueriedDateTime() {
        return lastQueriedDateTime;
    }

    public void updateLastQueriedDateTime(LocalDateTime dateTime) {
        this.lastQueriedDateTime = dateTime;
    }
}
