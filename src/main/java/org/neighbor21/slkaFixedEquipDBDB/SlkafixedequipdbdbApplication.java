package org.neighbor21.slkaFixedEquipDBDB;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
//@EnableScheduling 어노테이션은 스프링의 스케줄링 기능을 활성화하는 데 사용됩니다. 이 어노테이션이 메인 클래스에 추가되면, 스프링 부트는 애플리케이션 내에서 @Scheduled 어노테이션이 붙은 메소드를 찾아 해당 메소드를 정의된 스케줄에 따라 자동으로 실행합니다
public class SlkafixedequipdbdbApplication {

    public static void main(String[] args) {
        SpringApplication.run(SlkafixedequipdbdbApplication.class, args);
    }


}
