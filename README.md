# SLKA-FixedEquip-DBDB 프로젝트

## 프로젝트 개요
SLKA-FixedEquip-DBDB 프로젝트는 PostgreSQL 데이터베이스를 사용하여 대규모 데이터를 처리하고 관리하는 시스템입니다. 이 프로젝트는 두 개의 데이터 소스(Primary와 Secondary)를 설정하고, 데이터를 주기적으로 조회하여 변환 및 저장하는 작업을 수행합니다. 또한, 데이터베이스 연결을 유지하고, 대량의 데이터를 배치로 삽입하며, 재시도 로직을 통해 데이터 전송 실패 시 복구할 수 있도록 설계되었습니다.

## 기술 스택
- **Java 17**: 최신 Java 기능을 활용하여 개발되었습니다.
- **Spring Boot 3.1.10-SNAPSHOT**: 스프링 부트를 사용하여 애플리케이션의 빠른 개발과 배포를 지원합니다.
- **Spring Data JPA**: JPA(Java Persistence API)를 사용하여 데이터베이스 연산을 수행합니다.
- **PostgreSQL**: 오픈 소스 관계형 데이터베이스 관리 시스템으로, 프로젝트의 데이터 저장소로 사용됩니다.
- **Logback**: 로깅을 위해 사용됩니다.
- **Resilience4j**: 재시도 로직 및 지수 백오프를 구현하기 위해 사용됩니다.
- **HikariCP**: 고성능 JDBC Connection Pool을 제공하여 데이터베이스 연결을 효율적으로 관리합니다.
- **Lombok**: 반복되는 코드 생성을 줄이기 위해 사용됩니다.
- **Hibernate**: Hibernate Query Language (HQL): Hibernate에서 사용하는 객체 지향 쿼리 언어.
## 프로젝트 구조
src/main/java
|-- org/neighbor21/slkaFixedEquipDBDB
|-- config
| |-- Constants.java
| |-- PrimaryDataSourceConfig.java
| |-- SecondaryDataSourceConfig.java
| |-- TransactionManagerConfig.java
|-- dto
| |-- TL_VDS_PASSDto.java
| |-- TL_VDS_PASSKeyDto.java
|-- entity
| |-- compositekey
| | |-- TL_VDS_PASSKey.java
| | |-- Tms_TrackingKey.java
| |-- primary
| | |-- Tms_Tracking.java
| |-- secondary
| |-- TL_VDS_PASS.java
|-- handler
| |-- SaveVdsEntity.java
|-- jpareposit
| |-- primaryRepo
| | |-- TmsTrackingReposit.java
| |-- secondaryRepo
| |-- TlVdsPassReposit.java
|-- service
| |-- BatchService.java
| |-- DataTransferService.java
| |-- DatabaseConnectionKeeper.java
| |-- LastQueriedTimeService.java
|-- util
| |-- DatabaseConnectionLogger.java
| |-- ParsingUtil.java
|-- SlkafixedequipdbdbApplication.java

## 주요 클래스 및 구성 요소

### 1. Constants.java
상수 값을 미리 지정해놓는 클래스입니다. `DEFAULT_BATCH_SIZE`와 같은 상수 값을 정의하여 사용합니다.

### 2. 데이터 소스 구성
- **PrimaryDataSourceConfig.java**
    - Primary 데이터 소스를 설정하고, JPA EntityManager와 트랜잭션 매니저를 구성하는 클래스입니다.
    - `@Primary`: 우선순위가 높은 빈을 지정합니다.
    - `@Bean(name = "primaryDataSource")`: 기본 데이터 소스를 설정합니다.
    - `@ConfigurationProperties(prefix = "spring.datasource.primary")`: `application.properties` 파일의 설정을 사용합니다.

- **SecondaryDataSourceConfig.java**
    - Secondary 데이터 소스를 설정하고, JPA EntityManager와 트랜잭션 매니저를 구성하는 클래스입니다.
    - `@Bean(name = "secondaryDataSource")`: 보조 데이터 소스를 설정합니다.
    - `@ConfigurationProperties(prefix = "spring.datasource.secondary")`: `application.properties` 파일의 설정을 사용합니다.

- **TransactionManagerConfig.java**
    - Primary 및 Secondary 데이터 소스에 대한 트랜잭션 관리자를 설정하는 클래스입니다.
    - `@Primary`: 우선순위가 높은 트랜잭션 관리자를 지정합니다.
    - `@Bean(name = "primaryTransactionManager")`: 기본 트랜잭션 관리자를 설정합니다.
    - `@Bean(name = "secondaryTransactionManager")`: 보조 트랜잭션 관리자를 설정합니다.

### 3. 엔티티 및 DTO
- **TL_VDS_PASS.java**
    - Secondary 데이터베이스에 적재할 엔티티 클래스입니다. 복합 키를 사용하며, `@SQLInsert` 어노테이션을 통해 데이터 삽입 시 중복 키를 무시합니다.

- **Tms_Tracking.java**
    - Primary 데이터베이스에서 가져올 엔티티 클래스입니다. 복합 키를 사용합니다.

- **TL_VDS_PASSDto.java**
    - `TL_VDS_PASS` 엔티티에 대한 DTO(Data Transfer Object) 클래스입니다. 데이터를 변환하고 전송하는 데 사용됩니다.

- **TL_VDS_PASSKeyDto.java**
    - `TL_VDS_PASS` 엔티티의 복합 키에 대한 DTO 클래스입니다.

### 4. JPA 리포지토리
- **TmsTrackingReposit.java**
    - Primary 데이터베이스의 `Tms_Tracking` 엔티티에 대한 JPA 리포지토리 인터페이스입니다.
    - `@Query`: 마지막 조회 시간 이후의 새로운 데이터를 조회하는 메소드를 정의합니다.

- **TlVdsPassReposit.java**
    - Secondary 데이터베이스의 `TL_VDS_PASS` 엔티티에 대한 JPA 리포지토리 인터페이스입니다.
    - `@Query`: 주어진 키 세트에 해당하는 존재하는 키들을 조회하는 메소드를 정의합니다.

### 5. 서비스
- **BatchService.java**
    - 대량의 `TL_VDS_PASS` 엔티티를 배치로 삽입하는 서비스 클래스입니다. 배치 작업 중 발생할 수 있는 예외를 처리하며 재시도를 지원합니다.
    - `batchInsertWithRetry(List<TL_VDS_PASS> entities)`: 주어진 엔티티 리스트를 배치로 삽입하는 메소드입니다.

- **DataTransferService.java**
    - Primary 테이블에서 Secondary 테이블로 데이터를 옮기기 위해 타입 변환 및 파싱 후 적재하는 로직을 담당하는 서비스 클래스입니다.
    - `transferData(List<Tms_Tracking> newDataList)`: 데이터를 전송하는 메소드입니다.
    - `validateData(Tms_Tracking sourceData)`: 데이터를 유효성 검사하는 메소드입니다.
    - `retryFailedData(Tms_Tracking failedData, int retryCount)`: 재시도 로직을 수행하는 메소드입니다.
    - `convertEntityToDTO(Tms_Tracking entity)`: 엔티티를 DTO로 변환하는 메소드입니다.
    - `convertDtoToEntity(TL_VDS_PASSDto dto)`: DTO를 엔티티로 변환하는 메소드입니다.

### 6. 데이터베이스 연결 유지
- **DatabaseConnectionKeeper.java**
    - 데이터베이스 연결을 유지하기 위한 클래스입니다. 주기적으로 데이터베이스에 간단한 쿼리를 실행하여 연결을 유지합니다.
    - `keepAlivePrimaryDataSource()`: 기본 데이터 소스의 연결을 유지합니다.
    - `keepAliveSecondaryDataSource()`: 보조 데이터 소스의 연결을 유지합니다.
    - `keepAliveConnection(DataSource dataSource, String dataSourceName)`: 주어진 데이터 소스에 대해 연결을 유지하는 쿼리를 실행합니다.

### 7. 마지막 조회 시간 관리
- **LastQueriedTimeService.java**
    - 마지막 조회 시간을 관리하는 서비스 클래스입니다. 파일에 마지막 조회 시간을 저장하고 읽어옵니다.
    - `updateLastQueriedDateTime(LocalDateTime dateTime)`: 마지막 조회 시간을 업데이트하고 파일에 저장합니다.
    - `readLastQueriedDateTime()`: 파일에서 마지막 조회 시간을 읽어옵니다.
    - `writeLastQueriedDateTime()`: 마지막 조회 시간을 파일에 저장합니다.
    - `createFileWithDefaultTime()`: 기본 시간을 설정하고 파일을 생성합니다.
    - `getLastQueriedDateTime()`: 마지막 조회 시간을 반환합니다.

### 8. 유틸리티 클래스
- **DatabaseConnectionLogger.java**
    - 데이터베이스 연결의 지속을 위해 쿼리 1을 실행하는 유틸리티 클래스입니다. 애플리케이션 시작 시 데이터베이스 연결을 테스트하고 결과를 로그로 기록합니다.

- **ParsingUtil.java**
    - 데이터 파싱을 위한 유틸리티 클래스입니다. 차량 분류 코드에 맞춰서 차량 종류로 분류하는 메소드를 제공합니다.
    - `getVehicleClassification(int labelId)`: 차량 분류 코드에 맞춰서 차량 종류로 분류하는 메소드입니다.
