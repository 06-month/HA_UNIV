# 네이버 클라우드 플랫폼 (NCP) 배포 가이드

## 프라이빗 VPC 내 MySQL 서버 연동

### 1. 사전 준비

#### 필요한 정보
- MySQL 서버의 VPC 내부 IP 주소
- 데이터베이스 이름: `univ_db`
- 데이터베이스 사용자명 및 비밀번호
- 애플리케이션 서버의 VPC 내부 IP 주소

### 2. 보안 그룹 설정

#### MySQL 서버 보안 그룹 설정

1. **네이버 클라우드 콘솔** 접속
2. **Server (서버)** → 해당 MySQL 서버 선택
3. **보안 그룹** 탭 클릭
4. **인바운드 규칙 추가**:
   ```
   프로토콜: TCP
   포트: 3306
   소스: [애플리케이션 서버 보안 그룹] 또는 [VPC 내부 IP 대역]
   설명: 애플리케이션 서버에서 MySQL 접근
   ```

#### ACG (Access Control Group) 설정

1. **Server (서버)** → **ACG** 메뉴
2. MySQL 서버의 ACG 선택
3. **인바운드 규칙 추가**:
   - 애플리케이션 서버의 IP 주소 또는 보안 그룹 추가

### 3. 환경 변수 설정

#### 방법 1: 서버 시작 스크립트에 추가

**네이버 클라우드 콘솔 → Server → 사용자 스크립트:**

```bash
#!/bin/bash

# 데이터베이스 연결 정보
export DB_URL=jdbc:mysql://[MySQL서버VPC내부IP]:3306/univ_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export SPRING_PROFILES_ACTIVE=prod

# 애플리케이션 실행
cd /app
java -jar grade-inquiry-backend-1.0.0.jar
```

#### 방법 2: /etc/environment 파일에 추가

```bash
sudo nano /etc/environment
```

다음 내용 추가:
```
DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
DB_USERNAME=your_username
DB_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=prod
```

#### 방법 3: systemd 서비스 파일에 추가

```bash
sudo nano /etc/systemd/system/grade-inquiry.service
```

```ini
[Unit]
Description=Grade Inquiry System
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/app
Environment="DB_URL=jdbc:mysql://10.0.1.10:3306/univ_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
Environment="DB_USERNAME=your_username"
Environment="DB_PASSWORD=your_password"
Environment="SPRING_PROFILES_ACTIVE=prod"
ExecStart=/usr/bin/java -jar /app/grade-inquiry-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

서비스 활성화:
```bash
sudo systemctl daemon-reload
sudo systemctl enable grade-inquiry
sudo systemctl start grade-inquiry
```

### 4. VPC 내부 IP 확인

#### MySQL 서버 IP 확인

**방법 1: 네이버 클라우드 콘솔**
1. Server (서버) → MySQL 서버 선택
2. 상세 정보 → 네트워크 인터페이스
3. VPC 내부 IP 확인 (예: 10.0.1.10)

**방법 2: SSH 접속 후 확인**
```bash
# MySQL 서버에 SSH 접속
ssh user@mysql-server-ip

# IP 주소 확인
ip addr show | grep "inet " | grep -v 127.0.0.1
# 또는
hostname -I
```

### 5. 연결 테스트

#### 애플리케이션 서버에서 테스트

```bash
# 1. 포트 연결 확인
telnet [MySQL서버VPC내부IP] 3306

# 2. MySQL 클라이언트로 연결 테스트
mysql -h [MySQL서버VPC내부IP] -u your_username -p univ_db

# 3. 애플리케이션 헬스 체크
curl http://localhost:8080/actuator/health
```

### 6. 데이터베이스 스키마 마이그레이션

#### 로컬에서 스키마 덤프

```bash
# 로컬 데이터베이스에서 스키마 덤프
mysqldump -u grade_user -p grade_portal --no-data > schema.sql

# 또는 데이터 포함
mysqldump -u grade_user -p grade_portal > full_dump.sql
```

#### 네이버 클라우드 MySQL 서버에 적용

```bash
# SSH 터널링을 통한 접속 (공개 IP가 있는 경우)
mysql -h [MySQL서버공개IP] -u your_username -p univ_db < schema.sql

# 또는 VPC 내부에서 직접 접속
# 애플리케이션 서버에서 MySQL 서버로 직접 접속
mysql -h [MySQL서버VPC내부IP] -u your_username -p univ_db < schema.sql
```

### 7. 애플리케이션 배포

#### JAR 파일 업로드 및 실행

```bash
# 1. 애플리케이션 서버에 JAR 파일 업로드
scp backend/build/libs/grade-inquiry-backend-1.0.0.jar user@[애플리케이션서버IP]:/app/

# 2. SSH 접속
ssh user@[애플리케이션서버IP]

# 3. 환경 변수 설정
export DB_URL=jdbc:mysql://[MySQL서버VPC내부IP]:3306/univ_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export SPRING_PROFILES_ACTIVE=prod

# 4. 애플리케이션 실행
cd /app
java -jar grade-inquiry-backend-1.0.0.jar
```

### 8. 체크리스트

- [ ] MySQL 서버의 VPC 내부 IP 확인
- [ ] 데이터베이스 `univ_db` 생성 확인
- [ ] 데이터베이스 사용자 계정 생성 및 권한 설정
- [ ] MySQL 서버 보안 그룹에 인바운드 규칙 추가 (포트 3306)
- [ ] ACG 설정 확인
- [ ] 애플리케이션 서버에서 MySQL 서버로 연결 테스트
- [ ] 스키마 및 초기 데이터 마이그레이션
- [ ] 환경 변수 설정
- [ ] 애플리케이션 실행 및 헬스 체크

### 9. 트러블슈팅

#### 연결 실패 시 확인 사항

1. **보안 그룹 설정 확인**
   ```bash
   # 애플리케이션 서버에서 포트 확인
   telnet [MySQL서버VPC내부IP] 3306
   ```

2. **MySQL 서버 상태 확인**
   ```bash
   # MySQL 서버에서
   sudo systemctl status mysql
   sudo netstat -tlnp | grep 3306
   ```

3. **방화벽 설정 확인**
   ```bash
   # MySQL 서버에서
   sudo ufw status
   sudo iptables -L -n
   ```

4. **애플리케이션 로그 확인**
   ```bash
   # 애플리케이션 서버에서
   tail -f /var/log/grade-inquiry.log
   # 또는
   journalctl -u grade-inquiry -f
   ```

### 10. 보안 권장사항

1. **VPC 내부 통신 사용**
   - 공개 IP 대신 VPC 내부 IP 사용
   - SSL 연결은 선택사항 (VPC 내부이므로)

2. **보안 그룹 최소 권한 원칙**
   - 애플리케이션 서버에서만 접근 가능하도록 설정
   - 불필요한 포트는 차단

3. **비밀번호 관리**
   - 환경 변수에 직접 저장하지 말고, 네이버 클라우드 Vault 서비스 사용 고려
   - 또는 암호화된 설정 파일 사용

4. **정기적인 보안 업데이트**
   - MySQL 서버 및 애플리케이션 서버 보안 패치 적용
