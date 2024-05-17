package org.neighbor21.slkaFixedEquipDBDB.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * packageName    : org.neighbor21.slkaFixedEquipDBDB.config
 * fileName       : TransactionManagerConfig.java
 * author         : kjg08
 * date           : 2024-05-16
 * description    : Primary 및 Secondary 데이터 소스에 대한 트랜잭션 관리자를 설정하는 클래스. 이를 통해 각 데이터 소스에 대해 트랜잭션을 관리하고, 데이터베이스 연산의 일관성을 보장합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-05-16        kjg08           최초 생성
 */

@EnableTransactionManagement // Spring 선언적 트랜잭션 관리 활성화
public class TransactionManagerConfig {

    /**
     * 기본 트랜잭션 관리자를 생성.
     *
     * @param emf 기본 EntityManagerFactory 객체
     * @return PlatformTransactionManager 객체
     */
    @Primary // 여러 개의 트랜잭션 관리자 중 우선순위를 지정
    @Bean(name = "primaryTransactionManager") // Spring 컨테이너에 관리되는 빈 생성. 메소드 이름이 빈의 ID가 됨
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    /**
     * 보조 트랜잭션 관리자를 생성.
     *
     * @param emf 보조 EntityManagerFactory 객체
     * @return PlatformTransactionManager 객체
     */
    @Bean(name = "secondaryTransactionManager") // Spring 컨테이너에 관리되는 빈 생성. 메소드 이름이 빈의 ID가 됨
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}