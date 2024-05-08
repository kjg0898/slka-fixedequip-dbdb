package org.neighbor21.slkaFixedEquipDBDB.config;

import jakarta.persistence.EntityManagerFactory;
import org.neighbor21.slkaFixedEquipDBDB.Util.DatabaseConnectionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb
 * fileName       : secondaryDataSourceConfig.java
 * author         : kjg08
 * date           : 2024-04-03
 * description    : 적재할 테이블 설정 파일
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2024-04-03        kjg08           최초 생성
 */
@EnableConfigurationProperties
@Configuration //spring 의 설정파일임을 나타냄 이 클래스에서 선언된 bean 들은 spring 컨테이너에 의해 관리
@EnableTransactionManagement // spring 선언적 트랜잭션(데이터베이스의 상태를 변화시키기 해서 수행하는 작업의 단위) 자동 관리 활성화
@EnableJpaRepositories(  // spring data jpa 리포지토리를 스캔하여 spring 컨테이너에 등록
        basePackages = "org.neighbor21.slkaFixedEquipDBDB.jpareposit.secondary",
        entityManagerFactoryRef = "secondaryEntityManagerFactory",
        transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDataSourceConfig {
    private final Logger logger = LoggerFactory.getLogger(DatabaseConnectionLogger.class);

    @Bean(name = "secondaryDataSource") //spring 컨테이너에 관리되는 bean 생성, 메소드 이름이 bean 의 id
    @ConfigurationProperties(prefix = "spring.datasource.secondary") //application.properties 설정과 bean 의 속성 결정
    public DataSource secondaryDataSource() {  //데이터베이스 연결 정보를 가진 DataSource 객체를 생성하고 반환
        DataSource dataSource = DataSourceBuilder.create().build();
        logger.info(dataSource.toString());
        return dataSource;
    }

    @Bean(name = "secondaryEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(  //JPA EntityManagerFactory를 설정하는 메소드, 엔티티를 관리하고 JPA 오퍼레이션을 실행하는 데 필요
                                                                                  EntityManagerFactoryBuilder builder, @Qualifier("secondaryDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("org.neighbor21.slkafixedequipdbdb.entity.secondary")
                .persistenceUnit("secondary")
                .build();
    }

    @Bean(name = "secondaryTransactionManager")
    public PlatformTransactionManager secondaryTransactionManager( //트랜잭션 관리자를 생성 JPA 트랜잭션을 관리하기 위해 EntityManagerFactory를 사용
                                                                   @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}

