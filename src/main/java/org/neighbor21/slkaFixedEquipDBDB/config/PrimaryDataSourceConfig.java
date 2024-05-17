package org.neighbor21.slkaFixedEquipDBDB.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb
 * fileName       : PrimaryDataSourceConfig.java
 * author         : kjg08
 * date           : 2024-04-03
 * description    : Primary 데이터 소스 및 JPA 설정 클래스. 이 클래스는 기본 데이터 소스를 설정하고, JPA EntityManager와 트랜잭션 매니저를 구성합니다. 이를 통해 해당 데이터 소스에 대한 데이터베이스 연산 및 트랜잭션 관리를 처리합니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-03        kjg08           최초 생성
 */


@EnableConfigurationProperties
@Configuration // Spring의 설정 파일임을 나타냄. 이 클래스에서 선언된 빈들은 Spring 컨테이너에 의해 관리됨
@EnableTransactionManagement // Spring 선언적 트랜잭션 자동 관리 활성화
@EnableJpaRepositories( // Spring Data JPA 리포지토리를 스캔하여 Spring 컨테이너에 등록
        basePackages = "org.neighbor21.slkaFixedEquipDBDB.jpareposit.primaryRepo", // 기본 JPA 리포지토리 패키지 경로
        entityManagerFactoryRef = "primaryEntityManagerFactory", // 기본 엔티티 매니저 팩토리 참조 설정
        transactionManagerRef = "primaryTransactionManager" // 기본 트랜잭션 매니저 참조 설정
)
public class PrimaryDataSourceConfig {

    /**
     * 기본 데이터 소스 빈을 생성. Spring 컨테이너에 의해 관리됨.
     * application.properties 파일의 spring.datasource.primary 속성에 따라 설정됨.
     *
     * @return 기본 데이터 소스 객체
     */
    @Primary // 여러 개의 빈 중 우선순위를 지정
    @Bean(name = "primaryDataSource") // Spring 컨테이너에 관리되는 빈 생성. 메소드 이름이 빈의 ID가 됨
    @ConfigurationProperties(prefix = "spring.datasource.primary") // application.properties 설정과 빈의 속성 결정
    public DataSource primaryDataSource() { // 데이터베이스 연결 정보를 가진 DataSource 객체를 생성하고 반환
        return DataSourceBuilder.create().build();
    }

    /**
     * JPA EntityManagerFactory를 설정하는 메소드. 엔티티를 관리하고 JPA 연산을 실행하는 데 필요.
     *
     * @param builder    EntityManagerFactoryBuilder 객체
     * @param dataSource 기본 데이터 소스 객체
     * @return LocalContainerEntityManagerFactoryBean 객체
     */
    @Primary
    @Bean(name = "primaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("primaryDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource) // 데이터 소스 설정
                .packages("org.neighbor21.slkaFixedEquipDBDB.entity.primary") // JPA 엔티티 패키지 경로 설정
                .persistenceUnit("primary") // 영속성 유닛 이름 설정
                .build();
    }

    /**
     * 트랜잭션 관리자를 생성. JPA 트랜잭션을 관리하기 위해 EntityManagerFactory를 사용.
     *
     * @param primaryEntityManagerFactory 기본 엔티티 매니저 팩토리 빈
     * @return PlatformTransactionManager 객체
     */
    @Primary
    @Bean(name = "primaryTransactionManager")
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(primaryEntityManagerFactory.getObject()); // 엔티티 매니저 팩토리 설정
        return transactionManager;
    }
}

