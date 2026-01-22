# High-Availability Grade Inquiry System

성적 조회 시스템 - 고가용성 아키텍처 기반

## 프로젝트 구조

```
프로젝트 루트/
├── backend/                    # 백엔드 (Spring Boot)
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew                 # Gradle wrapper
│   └── src/main/
│       ├── java/com/university/grade/
│       │   ├── Application.java
│       │   ├── config/          # 설정 클래스
│       │   ├── controller/      # REST API 컨트롤러
│       │   ├── dto/            # 데이터 전송 객체
│       │   ├── entity/         # JPA 엔티티
│       │   ├── repository/     # JPA 리포지토리
│       │   ├── service/        # 비즈니스 로직
│       │   └── exception/      # 예외 처리
│       └── resources/
│           └── application.yml # 애플리케이션 설정
│
└── frontend/                   # 프론트엔드 (정적 파일)
    └── src/
        ├── login/             # 로그인 페이지
        │   ├── index.html
        │   ├── app.js
        │   └── styles.css
        └── main/              # 메인 페이지
            ├── index.html
            ├── app.js
            └── styles.css
```

## 실행 방법

### 1. Java 설치 확인
```bash
java -version
```
Java 17 이상이 필요합니다.

### 2. 데이터베이스 준비
MySQL 데이터베이스 `grade_portal`이 실행 중이어야 합니다.
- 호스트: localhost
- 포트: 3306
- 데이터베이스: grade_portal
- 사용자: grade_user
- 비밀번호: grade_password

### 3. Spring Session 테이블 생성
```sql
CREATE TABLE SPRING_SESSION (
    PRIMARY_ID CHAR(36) NOT NULL,
    SESSION_ID CHAR(36) NOT NULL,
    CREATION_TIME BIGINT NOT NULL,
    LAST_ACCESS_TIME BIGINT NOT NULL,
    MAX_INACTIVE_INTERVAL INT NOT NULL,
    EXPIRY_TIME BIGINT NOT NULL,
    PRINCIPAL_NAME VARCHAR(100),
    CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE UNIQUE INDEX SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID);
CREATE INDEX SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME);
CREATE INDEX SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME);

CREATE TABLE SPRING_SESSION_ATTRIBUTES (
    SESSION_PRIMARY_ID CHAR(36) NOT NULL,
    ATTRIBUTE_NAME VARCHAR(200) NOT NULL,
    ATTRIBUTE_BYTES BLOB NOT NULL,
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME),
    CONSTRAINT SPRING_SESSION_ATTRIBUTES_FK FOREIGN KEY (SESSION_PRIMARY_ID) REFERENCES SPRING_SESSION(PRIMARY_ID) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. 애플리케이션 실행

백엔드 디렉토리에서 실행:
```bash
cd backend
./gradlew bootRun
```

또는 루트에서:
```bash
./gradlew -p backend bootRun
```

#### 빌드 후 JAR 실행
```bash
cd backend
./gradlew build
java -jar build/libs/grade-inquiry-backend-1.0.0.jar
```

### 5. 접속
- 프론트엔드: http://localhost:8080/login/index.html
- API: http://localhost:8080/api/v1/...

## 주요 기능

1. **로그인**: 학번/사번으로 로그인 (세션 기반)
2. **성적 조회**: 모든 학기 성적 조회 (캐시 적용, TTL: 5분)
   - 학기 목록을 동적으로 로드
   - 선택한 학기의 성적 조회
3. **이의신청**: 성적 이의신청 제출

## API 엔드포인트

- `POST /api/v1/auth/login` - 로그인
- `POST /api/v1/auth/logout` - 로그아웃
- `GET /api/v1/grades/semesters` - 사용 가능한 학기 목록
- `GET /api/v1/grades/summary?semester={semester}` - 성적 요약
- `GET /api/v1/grades/list?semester={semester}` - 성적 목록
- `POST /api/v1/objections` - 이의신청

## 기술 스택

- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security
- Spring Session (JDBC)
- MySQL 8.0
- Caffeine Cache
- Gradle

## 테스트 계정

- 학번/아이디: `20240001`
- 비밀번호: `password123`
