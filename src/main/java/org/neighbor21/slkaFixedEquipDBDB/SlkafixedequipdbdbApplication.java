package org.neighbor21.slkaFixedEquipDBDB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB
 * fileName       : SlkafixedequipdbdbApplication.java
 * author         : kjg08
 * date           : 2024-04-05
 * description    : Spring Boot 애플리케이션의 시작 클래스. 스케줄링 기능과 엔티티 스캔을 활성화함.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-05        kjg08           최초 생성
 */

@SpringBootApplication // Spring Boot 애플리케이션의 시작 클래스임을 나타냄
@EnableScheduling // Spring의 스케줄링 기능을 활성화
//@EnableScheduling 어노테이션은 스프링의 스케줄링 기능을 활성화하는 데 사용됩니다. 이 어노테이션이 메인 클래스에 추가되면, 스프링 부트는 애플리케이션 내에서 @Scheduled 어노테이션이 붙은 메소드를 찾아 해당 메소드를 정의된 스케줄에 따라 자동으로 실행합니다
@EntityScan(basePackages = "org.neighbor21.slkaFixedEquipDBDB.entity") // JPA 엔티티 스캔 패키지 설정
public class SlkafixedequipdbdbApplication {

    /**
     * 애플리케이션의 메인 메소드. Spring Boot 애플리케이션을 실행함.
     *
     * @param args 커맨드 라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(SlkafixedequipdbdbApplication.class, args);
    }
}