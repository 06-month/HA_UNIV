# 클라우드 배포 가이드

## 데이터베이스 설정 방법

### 방법 1: 환경 변수 사용 (권장)

클라우드 환경에서 환경 변수를 설정하여 데이터베이스 연결 정보를 제공합니다.

#### 필수 환경 변수

```bash
# 데이터베이스 URL (예: AWS RDS, Google Cloud SQL, Azure Database 등)
DB_URL=jdbc:mysql://your-db-host:3306/grade_portal

# 데이터베이스 사용자명
DB_USERNAME=your_db_username

# 데이터베이스 비밀번호
DB_PASSWORD=your_db_password

# 프로덕션 프로파일 활성화
SPRING_PROFILES_ACTIVE=prod
```

#### 선택적 환경 변수

```bash
# SSL 사용 여부 (프로덕션에서는 true 권장)
DB_USE_SSL=true

# Public Key Retrieval 허용 여부
DB_ALLOW_PUBLIC_KEY=false

# 연결 풀 설정
DB_MAX_POOL_SIZE=20
DB_MIN_IDLE=10
DB_CONNECTION_TIMEOUT=30000

# 서버 포트
SERVER_PORT=8080

# 쿠키 보안 설정 (HTTPS 환경에서는 true)
COOKIE_SECURE=true

# CORS 허용 오리진 (쉼표로 구분)
CORS_ALLOWED_ORIGINS=https://your-domain.com,https://www.your-domain.com
```

### 방법 2: application-prod.yml 파일 직접 수정

`backend/src/main/resources/application-prod.yml` 파일을 직접 수정할 수 있습니다.

## 주요 클라우드 플랫폼별 설정 예시

### AWS (Elastic Beanstalk / EC2)

#### Elastic Beanstalk 환경 변수 설정

1. AWS 콘솔 → Elastic Beanstalk → 환경 → 구성 → 소프트웨어
2. 환경 속성 추가:
   ```
   DB_URL=jdbc:mysql://your-rds-endpoint.region.rds.amazonaws.com:3306/grade_portal
   DB_USERNAME=admin
   DB_PASSWORD=your_secure_password
   SPRING_PROFILES_ACTIVE=prod
   ```

#### EC2 인스턴스에서 환경 변수 설정

```bash
# /etc/environment 파일에 추가
sudo nano /etc/environment

# 또는 systemd 서비스 파일에 추가
sudo nano /etc/systemd/system/grade-inquiry.service
```

### Google Cloud Platform (Cloud Run / App Engine)

#### Cloud Run 환경 변수

```bash
gcloud run deploy grade-inquiry \
  --set-env-vars="DB_URL=jdbc:mysql://your-sql-instance:3306/grade_portal" \
  --set-env-vars="DB_USERNAME=root" \
  --set-env-vars="DB_PASSWORD=your_password" \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod"
```

#### App Engine (app.yaml)

```yaml
env_variables:
  DB_URL: jdbc:mysql://your-sql-instance:3306/grade_portal
  DB_USERNAME: root
  DB_PASSWORD: your_password
  SPRING_PROFILES_ACTIVE: prod
```

### Azure (App Service)

#### App Service 설정

1. Azure Portal → App Service → 구성 → 애플리케이션 설정
2. 새 애플리케이션 설정 추가:
   ```
   DB_URL = jdbc:mysql://your-server.mysql.database.azure.com:3306/grade_portal
   DB_USERNAME = admin@your-server
   DB_PASSWORD = your_password
   SPRING_PROFILES_ACTIVE = prod
   ```

### 네이버 클라우드 플랫폼 (NCP) - 프라이빗 VPC

#### VPC 내부 MySQL 서버 연동

네이버 클라우드의 프라이빗 VPC 환경에서는 VPC 내부 IP 주소를 사용하여 MySQL 서버에 연결합니다.

##### 1. 환경 변수 설정

**Server (서버) 환경에서:**
```bash
# VPC 내부 IP 주소 사용 (예: 10.0.1.10)
DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8

# 데이터베이스 사용자명
DB_USERNAME=your_db_username

# 데이터베이스 비밀번호
DB_PASSWORD=your_db_password

# 프로덕션 프로파일 활성화
SPRING_PROFILES_ACTIVE=prod
```

**또는 간단한 형식:**
```bash
DB_URL=jdbc:mysql://[VPC내부IP]:3306/univ_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=prod
```

##### 2. 네이버 클라우드 콘솔에서 설정

**Server (서버) → 서버 관리 → 환경 변수 설정:**

1. 네이버 클라우드 콘솔 접속
2. Server (서버) → 해당 서버 선택
3. 환경 변수 또는 시작 스크립트에 추가:
   ```bash
   export DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   export SPRING_PROFILES_ACTIVE=prod
   ```

##### 3. 보안 그룹 설정 (중요!)

**네이버 클라우드 콘솔에서:**

1. **Server (서버) → 보안 그룹**
2. MySQL 서버의 보안 그룹에 인바운드 규칙 추가:
   - **프로토콜**: TCP
   - **포트**: 3306
   - **소스**: 애플리케이션 서버의 보안 그룹 또는 VPC 내부 IP 대역
   - **설명**: "애플리케이션 서버에서 MySQL 접근 허용"

3. **ACG (Access Control Group) 설정:**
   - MySQL 서버 ACG에 애플리케이션 서버 IP 또는 보안 그룹 추가

##### 4. VPC 내부 IP 확인 방법

**MySQL 서버의 VPC 내부 IP 확인:**
```bash
# MySQL 서버에 SSH 접속 후
ip addr show | grep "inet " | grep -v 127.0.0.1

# 또는 네이버 클라우드 콘솔에서
# Server (서버) → 서버 상세 정보 → 네트워크 인터페이스
```

##### 5. 연결 테스트

애플리케이션 서버에서 MySQL 연결 테스트:
```bash
# MySQL 클라이언트로 연결 테스트
mysql -h 10.0.1.10 -u your_username -p univ_db

# 또는 telnet으로 포트 확인
telnet 10.0.1.10 3306
```

##### 6. 애플리케이션 실행

```bash
# 환경 변수 설정 후 실행
export DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export SPRING_PROFILES_ACTIVE=prod

java -jar grade-inquiry-backend-1.0.0.jar
```

##### 7. 네이버 클라우드 특화 설정

**프라이빗 VPC 환경 특성:**
- VPC 내부 IP 사용 (공개 IP 불필요)
- 보안 그룹/ACG로 접근 제어
- SSL 연결은 선택사항 (VPC 내부이므로)
- `useSSL=false` 권장 (성능 향상)

**예시 설정:**
```bash
# 프라이빗 VPC 환경 (SSL 불필요)
DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
DB_USE_SSL=false
DB_ALLOW_PUBLIC_KEY=true
```

### Docker 배포

#### Dockerfile 예시

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY backend/build/libs/grade-inquiry-backend-1.0.0.jar app.jar

# 환경 변수 설정 (또는 docker-compose.yml에서 설정)
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=jdbc:mysql://db:3306/grade_portal
ENV DB_USERNAME=grade_user
ENV DB_PASSWORD=grade_password

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.yml 예시

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:mysql://db:3306/grade_portal
      - DB_USERNAME=grade_user
      - DB_PASSWORD=grade_password
    depends_on:
      - db
  
  db:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=root_password
      - MYSQL_DATABASE=grade_portal
      - MYSQL_USER=grade_user
      - MYSQL_PASSWORD=grade_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```

## 데이터베이스 연결 문자열 형식

### MySQL/MariaDB

```
jdbc:mysql://[호스트]:[포트]/[데이터베이스명]?useSSL=[true/false]&serverTimezone=UTC&allowPublicKeyRetrieval=[true/false]&characterEncoding=UTF-8
```

### 예시

```bash
# 로컬 개발
DB_URL=jdbc:mysql://localhost:3306/grade_portal

# AWS RDS
DB_URL=jdbc:mysql://mydb.123456789012.us-east-1.rds.amazonaws.com:3306/grade_portal

# Google Cloud SQL
DB_URL=jdbc:mysql:///grade_portal?cloudSqlInstance=project:region:instance&socketFactory=com.google.cloud.sql.mysql.SocketFactory

# Azure Database for MySQL
DB_URL=jdbc:mysql://your-server.mysql.database.azure.com:3306/grade_portal?useSSL=true&requireSSL=true

# 네이버 클라우드 프라이빗 VPC
DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
```

## 보안 권장사항

1. **비밀번호 관리**
   - 환경 변수에 직접 저장하지 말고, 클라우드 시크릿 관리 서비스 사용
   - AWS: Secrets Manager, Parameter Store
   - GCP: Secret Manager
   - Azure: Key Vault

2. **SSL/TLS 연결**
   - 프로덕션 환경에서는 반드시 SSL 사용
   - `DB_USE_SSL=true` 설정

3. **방화벽 설정**
   - 데이터베이스는 애플리케이션 서버에서만 접근 가능하도록 설정
   - 공개 IP 비활성화 또는 IP 화이트리스트 설정

## 애플리케이션 실행

### JAR 파일로 실행

```bash
java -jar grade-inquiry-backend-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=$DB_URL \
  --spring.datasource.username=$DB_USERNAME \
  --spring.datasource.password=$DB_PASSWORD
```

### 환경 변수로 실행

```bash
export DB_URL=jdbc:mysql://your-db-host:3306/grade_portal
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export SPRING_PROFILES_ACTIVE=prod

java -jar grade-inquiry-backend-1.0.0.jar
```

## 데이터베이스 마이그레이션

클라우드 데이터베이스에 스키마를 생성해야 합니다:

1. 로컬에서 스키마 덤프:
```bash
mysqldump -u grade_user -p grade_portal --no-data > schema.sql
```

2. 클라우드 데이터베이스에 적용:
```bash
mysql -h your-db-host -u your_username -p grade_portal < schema.sql
```

## 체크리스트

- [ ] 데이터베이스 호스트, 포트, 데이터베이스명 확인
- [ ] 데이터베이스 사용자 계정 생성 및 권한 설정
- [ ] 방화벽 규칙 설정 (애플리케이션 서버 IP 허용)
- [ ] SSL 인증서 설정 (프로덕션)
- [ ] 환경 변수 설정
- [ ] 스키마 및 초기 데이터 마이그레이션
- [ ] 연결 테스트
- [ ] 보안 그룹/방화벽 규칙 확인
