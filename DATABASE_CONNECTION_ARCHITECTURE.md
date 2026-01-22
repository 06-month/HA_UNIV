# MySQL과 코드 연결 구조

## 전체 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    프론트엔드 (Frontend)                      │
│  - HTML/CSS/JavaScript                                      │
│  - API 호출: /api/v1/auth/login                             │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP Request
                       ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot 애플리케이션 (Backend)               │
│                                                              │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Controller Layer                                  │    │
│  │  - AuthController                                  │    │
│  │  - GradeController                                 │    │
│  │  - ObjectionController                             │    │
│  └──────────────┬─────────────────────────────────────┘    │
│                 │                                          │
│  ┌──────────────▼─────────────────────────────────────┐    │
│  │  Service Layer                                     │    │
│  │  - AuthService                                     │    │
│  │  - GradeInquiryService                             │    │
│  │  - ObjectionService                                │    │
│  └──────────────┬─────────────────────────────────────┘    │
│                 │                                          │
│  ┌──────────────▼─────────────────────────────────────┐    │
│  │  Repository Layer (JPA)                           │    │
│  │  - UserRepository                                 │    │
│  │  - StudentRepository                              │    │
│  │  - EnrollmentRepository                            │    │
│  │  - GradeRepository                                │    │
│  └──────────────┬─────────────────────────────────────┘    │
│                 │                                          │
│  ┌──────────────▼─────────────────────────────────────┐    │
│  │  Entity Layer (ORM Mapping)                       │    │
│  │  - User (@Entity)                                 │    │
│  │  - Student (@Entity)                              │    │
│  │  - Enrollment (@Entity)                           │    │
│  │  - Grade (@Entity)                                │    │
│  └──────────────┬─────────────────────────────────────┘    │
│                 │                                          │
│  ┌──────────────▼─────────────────────────────────────┐    │
│  │  Hibernate (ORM Framework)                         │    │
│  │  - Entity → SQL 변환                               │    │
│  │  - SQL 실행                                        │    │
│  └──────────────┬─────────────────────────────────────┘    │
│                 │                                          │
│  ┌──────────────▼─────────────────────────────────────┐    │
│  │  HikariCP (Connection Pool)                        │    │
│  │  - 연결 풀 관리                                    │    │
│  │  - 최대 10개 연결 (기본값)                        │    │
│  └──────────────┬─────────────────────────────────────┘    │
│                 │                                          │
│  ┌──────────────▼─────────────────────────────────────┐    │
│  │  MySQL JDBC Driver                                │    │
│  │  - com.mysql.cj.jdbc.Driver                       │    │
│  │  - JDBC 프로토콜로 MySQL과 통신                   │    │
│  └──────────────┬─────────────────────────────────────┘    │
└──────────────────┼────────────────────────────────────────┘
                   │ JDBC Protocol (TCP/IP)
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                    MySQL Database Server                     │
│  - Host: localhost (또는 환경 변수 DB_URL)                  │
│  - Port: 3306                                               │
│  - Database: grade_portal (또는 univ_db)                    │
│  - User: grade_user (또는 환경 변수 DB_USERNAME)            │
│  - Password: grade_password (또는 환경 변수 DB_PASSWORD)    │
│                                                              │
│  ┌──────────────┬──────────────┬──────────────┐           │
│  │   USERS      │  STUDENTS    │ ENROLLMENTS  │           │
│  │   table      │   table      │    table     │           │
│  └──────────────┴──────────────┴──────────────┘           │
│  ┌──────────────┬──────────────┬──────────────┐           │
│  │   GRADES     │GRADE_SUMMARY │GRADE_OBJECTIONS│          │
│  │   table      │   table      │    table     │           │
│  └──────────────┴──────────────┴──────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

## 1. 의존성 구조 (build.gradle)

### 핵심 라이브러리

```gradle
// Spring Boot Data JPA - JPA/Hibernate 포함
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

// MySQL Connector - JDBC 드라이버
runtimeOnly 'com.mysql:mysql-connector-j'

// Spring Session JDBC - 세션을 DB에 저장
implementation 'org.springframework.session:spring-session-jdbc'
```

**역할:**
- `spring-boot-starter-data-jpa`: JPA, Hibernate, HikariCP 자동 설정
- `mysql-connector-j`: MySQL과 통신하는 JDBC 드라이버
- `spring-session-jdbc`: 세션 데이터를 MySQL에 저장

## 2. 연결 설정 (application.yml)

### 데이터소스 설정

```yaml
spring:
  datasource:
    # JDBC URL - MySQL 서버 위치
    url: jdbc:mysql://localhost:3306/grade_portal?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
    
    # 인증 정보
    username: grade_user
    password: grade_password
    
    # JDBC 드라이버 클래스
    driver-class-name: com.mysql.cj.jdbc.Driver
    
    # HikariCP 연결 풀 설정
    hikari:
      maximum-pool-size: 10      # 최대 연결 수
      minimum-idle: 5            # 최소 유지 연결 수
      connection-timeout: 30000  # 연결 타임아웃 (30초)
```

### JPA/Hibernate 설정

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none  # 스키마 자동 생성 비활성화 (수동 관리)
    show-sql: true    # SQL 로그 출력
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect  # MySQL 8.0 방언
        format_sql: true
```

## 3. 연결 흐름 (Connection Flow)

### 애플리케이션 시작 시

```
1. Spring Boot 시작
   ↓
2. application.yml 읽기
   ↓
3. DataSource 빈 생성
   - HikariCP Connection Pool 초기화
   - MySQL JDBC Driver 로드
   ↓
4. MySQL 서버에 연결 시도
   - jdbc:mysql://localhost:3306/grade_portal
   - username: grade_user
   - password: grade_password
   ↓
5. 연결 풀에 연결 저장 (최소 5개)
   ↓
6. EntityManagerFactory 생성
   - Hibernate 초기화
   - Entity 매핑 정보 로드
```

### 실제 쿼리 실행 시

```
1. Service 메서드 호출
   예: authService.login(request)
   ↓
2. Repository 메서드 호출
   예: userRepository.findByLoginId(loginId)
   ↓
3. Hibernate가 SQL 생성
   SELECT * FROM USERS WHERE login_id = ?
   ↓
4. HikariCP에서 연결 가져오기
   - 풀에서 사용 가능한 연결 선택
   - 없으면 새로 생성 (최대 10개까지)
   ↓
5. MySQL JDBC Driver로 SQL 전송
   - TCP/IP 연결을 통해 MySQL 서버로 전송
   ↓
6. MySQL 서버에서 쿼리 실행
   ↓
7. 결과 반환
   ↓
8. Hibernate가 ResultSet을 Entity로 변환
   User 객체 생성
   ↓
9. 연결을 풀에 반환
   ↓
10. Service로 결과 반환
```

## 4. Entity와 테이블 매핑

### 예시: User Entity

```java
@Entity                    // JPA Entity로 표시
@Table(name = "USERS")     // MySQL 테이블명 매핑
public class User {
    @Id                    // Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT
    @Column(name = "user_id")  // 컬럼명 매핑
    private Long userId;
    
    @Column(name = "login_id", unique = true, nullable = false)
    private String loginId;
    
    @Column(name = "password_hash")
    private String passwordHash;
}
```

**매핑 관계:**
```
Java Entity (User)  ←→  MySQL Table (USERS)
├─ userId          ←→  user_id (BIGINT, PK, AUTO_INCREMENT)
├─ loginId         ←→  login_id (VARCHAR(50), UNIQUE, NOT NULL)
└─ passwordHash    ←→  password_hash (VARCHAR(255))
```

## 5. Repository 패턴

### Repository 인터페이스

```java
@Repository  // Spring이 자동으로 빈으로 등록
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository는 기본 CRUD 메서드 제공
    // - save(), findById(), findAll(), delete() 등
    
    // 커스텀 메서드 - Spring Data JPA가 자동으로 구현
    Optional<User> findByLoginId(String loginId);
    // → SELECT * FROM USERS WHERE login_id = ?
}
```

**Spring Data JPA의 자동 구현:**
- 메서드 이름을 분석하여 SQL 생성
- `findByLoginId` → `WHERE login_id = ?`
- `findByUserUserId` → `WHERE user_id = ?`

## 6. 실제 사용 예시

### 로그인 프로세스

```java
// 1. Controller에서 요청 받기
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(response);
}

// 2. Service에서 비즈니스 로직 처리
@Transactional(readOnly = true)
public LoginResponse login(LoginRequest request) {
    // 3. Repository를 통해 DB 조회
    Optional<User> userOpt = userRepository.findByLoginId(request.getUserId());
    // → Hibernate가 SQL 생성: SELECT * FROM USERS WHERE login_id = ?
    // → MySQL에서 실행
    // → 결과를 User 객체로 변환
    
    if (userOpt.isEmpty()) {
        throw new RuntimeException("학번/사번 또는 비밀번호가 올바르지 않습니다.");
    }
    
    User user = userOpt.get();
    // 비밀번호 검증 등...
}
```

## 7. 연결 풀 (Connection Pool) 동작

### HikariCP 연결 풀

```
┌─────────────────────────────────────┐
│      HikariCP Connection Pool      │
│                                     │
│  ┌─────┐  ┌─────┐  ┌─────┐        │
│  │Conn1│  │Conn2│  │Conn3│  ...   │
│  └──┬──┘  └──┬──┘  └──┬──┘        │
│     │       │       │              │
│     └───────┴───────┘              │
│            │                        │
│            ▼                        │
│     MySQL Server                    │
└─────────────────────────────────────┘
```

**동작 방식:**
1. 애플리케이션 시작 시 최소 5개 연결 생성
2. 쿼리 실행 시 풀에서 연결 가져오기
3. 쿼리 완료 후 연결을 풀에 반환
4. 최대 10개까지 연결 생성 가능
5. 30초 동안 사용되지 않으면 연결 해제

## 8. 환경 변수로 연결 정보 변경

### 로컬 개발 환경
```yaml
# application.yml (기본값)
datasource:
  url: jdbc:mysql://localhost:3306/grade_portal
  username: grade_user
  password: grade_password
```

### 클라우드 환경
```bash
# 환경 변수 설정
export DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db
export DB_USERNAME=cloud_user
export DB_PASSWORD=cloud_password
```

**우선순위:**
1. 환경 변수 (최우선)
2. application.yml 기본값

## 9. 주요 테이블 구조

### 테이블 관계도

```
USERS (사용자)
  │
  ├─ 1:1 → STUDENTS (학생 정보)
  │         │
  │         ├─ 1:N → ENROLLMENTS (수강 신청)
  │         │         │
  │         │         ├─ N:1 → COURSES (과목)
  │         │         │
  │         │         └─ 1:1 → GRADES (성적)
  │         │
  │         └─ 1:N → GRADE_SUMMARY (학기별 성적 요약)
  │
  └─ 1:N → GRADE_OBJECTIONS (이의신청)
            │
            └─ N:1 → ENROLLMENTS
```

## 10. 트랜잭션 관리

### @Transactional 어노테이션

```java
@Transactional(readOnly = true)  // 읽기 전용 트랜잭션
public LoginResponse login(LoginRequest request) {
    // 여러 Repository 호출이 하나의 트랜잭션으로 묶임
    User user = userRepository.findByLoginId(...);
    Student student = studentRepository.findByUserUserId(...);
    // 모두 같은 DB 연결 사용
}
```

**트랜잭션 동작:**
- `@Transactional`이 있으면 같은 DB 연결 사용
- 메서드 시작 시 트랜잭션 시작
- 메서드 종료 시 커밋 (또는 롤백)
- `readOnly = true`는 SELECT만 가능

## 요약

1. **설정**: `application.yml`에서 MySQL 연결 정보 설정
2. **드라이버**: `mysql-connector-j`가 JDBC 프로토콜로 MySQL과 통신
3. **연결 풀**: HikariCP가 연결을 관리하여 성능 최적화
4. **ORM**: Hibernate가 Java 객체와 SQL을 자동 변환
5. **Repository**: Spring Data JPA가 메서드 이름으로 SQL 자동 생성
6. **Entity**: Java 클래스가 MySQL 테이블과 매핑

이 구조로 개발자는 SQL을 직접 작성하지 않고도 Java 객체로 데이터베이스를 쉽게 다룰 수 있습니다.
