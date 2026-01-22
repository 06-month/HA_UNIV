# 쿠버네티스 환경 로그인 문제 해결 가이드

## 문제: ID와 Password가 맞는데도 "안맞다"고 나오는 경우

### 원인 분석

쿠버네티스 환경에서 ID/Password가 맞는데도 인증 실패가 발생하는 주요 원인:

1. **데이터베이스 연결 실패** (가장 흔한 원인)
   - DB 연결이 안되면 쿼리 자체가 실패
   - 예외가 "학번/사번 또는 비밀번호가 올바르지 않습니다."로 표시됨

2. **환경 변수 설정 문제**
   - ConfigMap/Secret에 DB 연결 정보가 제대로 설정되지 않음

3. **네트워크 연결 문제**
   - Pod에서 MySQL 서버로 접근 불가
   - Service/Ingress 설정 문제

4. **트랜잭션 롤백 문제**
   - @Transactional로 인한 예외 변환

## 해결 방법

### 1. 데이터베이스 연결 확인

#### Pod에서 직접 연결 테스트

```bash
# Pod에 접속
kubectl exec -it <pod-name> -n <namespace> -- /bin/sh

# 환경 변수 확인
echo $DB_URL
echo $DB_USERNAME
echo $DB_PASSWORD

# MySQL 연결 테스트 (mysql 클라이언트가 있는 경우)
mysql -h <mysql-host> -u $DB_USERNAME -p$DB_PASSWORD <database-name>
```

#### 애플리케이션 로그 확인

```bash
# Pod 로그 확인
kubectl logs <pod-name> -n <namespace> | grep -i "database\|connection\|error"

# 실시간 로그 확인
kubectl logs -f <pod-name> -n <namespace>
```

**확인할 로그:**
- `Database access error during login` - DB 연결 오류
- `User not found` - 사용자 없음 (정상적인 인증 실패)
- `password mismatch` - 비밀번호 불일치 (정상적인 인증 실패)

### 2. ConfigMap/Secret 설정 확인

#### ConfigMap 확인

```bash
# ConfigMap 확인
kubectl get configmap -n <namespace>
kubectl describe configmap <configmap-name> -n <namespace>

# ConfigMap 내용 확인
kubectl get configmap <configmap-name> -n <namespace> -o yaml
```

#### Secret 확인

```bash
# Secret 확인
kubectl get secret -n <namespace>
kubectl describe secret <secret-name> -n <namespace>

# Secret 값 확인 (base64 디코딩)
kubectl get secret <secret-name> -n <namespace> -o jsonpath='{.data.DB_PASSWORD}' | base64 -d
```

#### 올바른 설정 예시

**ConfigMap:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grade-inquiry-config
  namespace: default
data:
  DB_URL: "jdbc:mysql://mysql-service:3306/univ_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8"
  DB_USERNAME: "grade_user"
  SPRING_PROFILES_ACTIVE: "prod"
```

**Secret:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: grade-inquiry-secret
  namespace: default
type: Opaque
stringData:
  DB_PASSWORD: "your_password_here"
```

### 3. Deployment 설정 확인

#### 환경 변수 주입 확인

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grade-inquiry
spec:
  template:
    spec:
      containers:
      - name: grade-inquiry
        image: your-image:tag
        env:
        # ConfigMap에서 가져오기
        - name: DB_URL
          valueFrom:
            configMapKeyRef:
              name: grade-inquiry-config
              key: DB_URL
        - name: DB_USERNAME
          valueFrom:
            configMapKeyRef:
              name: grade-inquiry-config
              key: DB_USERNAME
        # Secret에서 가져오기
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: grade-inquiry-secret
              key: DB_PASSWORD
```

#### 환경 변수 확인 명령어

```bash
# Deployment의 환경 변수 확인
kubectl get deployment <deployment-name> -n <namespace> -o yaml | grep -A 20 "env:"

# 실행 중인 Pod의 환경 변수 확인
kubectl exec <pod-name> -n <namespace> -- env | grep DB_
```

### 4. 네트워크 연결 확인

#### MySQL Service 확인

```bash
# MySQL Service 확인
kubectl get svc -n <namespace> | grep mysql

# Service 상세 정보
kubectl describe svc <mysql-service-name> -n <namespace>

# Endpoint 확인
kubectl get endpoints <mysql-service-name> -n <namespace>
```

#### Pod에서 MySQL 연결 테스트

```bash
# Pod에서 telnet으로 포트 확인
kubectl exec <pod-name> -n <namespace> -- telnet <mysql-service-name> 3306

# 또는 nc (netcat) 사용
kubectl exec <pod-name> -n <namespace> -- nc -zv <mysql-service-name> 3306
```

#### DNS 확인

```bash
# Pod에서 DNS 확인
kubectl exec <pod-name> -n <namespace> -- nslookup <mysql-service-name>

# 또는 ping (ICMP가 허용된 경우)
kubectl exec <pod-name> -n <namespace> -- ping -c 3 <mysql-service-name>
```

### 5. 헬스 체크 확인

#### 애플리케이션 헬스 체크

```bash
# Pod IP 확인
kubectl get pod <pod-name> -n <namespace> -o wide

# 헬스 체크 API 호출
kubectl exec <pod-name> -n <namespace> -- curl http://localhost:8080/actuator/health

# 또는 Port Forward 사용
kubectl port-forward <pod-name> 8080:8080 -n <namespace>
curl http://localhost:8080/actuator/health
```

**정상 응답:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

**비정상 응답 (DB 연결 실패):**
```json
{
  "status": "DOWN",
  "components": {
    "db": {
      "status": "DOWN"
    }
  }
}
```

### 6. 로그 레벨 조정

#### application-prod.yml에 디버그 로그 추가

```yaml
logging:
  level:
    root: INFO
    com.university.grade: DEBUG
    org.springframework.jdbc: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### ConfigMap으로 로그 레벨 설정

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: grade-inquiry-config
data:
  LOGGING_LEVEL_COM_UNIVERSITY_GRADE: "DEBUG"
  LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_JDBC: "DEBUG"
```

### 7. 일반적인 문제 해결 체크리스트

#### ✅ 데이터베이스 연결
- [ ] MySQL 서버가 실행 중인가?
- [ ] MySQL Service가 올바르게 설정되었는가?
- [ ] Pod에서 MySQL Service로 접근 가능한가?
- [ ] 방화벽/NetworkPolicy가 연결을 차단하지 않는가?

#### ✅ 환경 변수
- [ ] ConfigMap/Secret이 올바르게 생성되었는가?
- [ ] Deployment에서 환경 변수가 올바르게 주입되었는가?
- [ ] Pod 내부에서 환경 변수가 올바른가?
- [ ] DB_URL 형식이 올바른가? (jdbc:mysql://...)

#### ✅ 인증 정보
- [ ] DB_USERNAME이 올바른가?
- [ ] DB_PASSWORD가 올바른가? (특수문자 이스케이프 확인)
- [ ] MySQL 사용자에게 필요한 권한이 있는가?

#### ✅ 애플리케이션
- [ ] 애플리케이션이 정상적으로 시작되었는가?
- [ ] 헬스 체크가 통과하는가?
- [ ] 로그에 에러가 없는가?

### 8. 디버깅 명령어 모음

```bash
# 1. Pod 상태 확인
kubectl get pods -n <namespace>

# 2. Pod 상세 정보
kubectl describe pod <pod-name> -n <namespace>

# 3. Pod 로그 확인
kubectl logs <pod-name> -n <namespace> --tail=100

# 4. Pod 내부 환경 변수 확인
kubectl exec <pod-name> -n <namespace> -- env | sort

# 5. Pod 내부에서 MySQL 연결 테스트
kubectl exec <pod-name> -n <namespace> -- sh -c 'echo "SELECT 1" | mysql -h <mysql-host> -u $DB_USERNAME -p$DB_PASSWORD'

# 6. ConfigMap 확인
kubectl get configmap <configmap-name> -n <namespace> -o yaml

# 7. Secret 확인 (base64 디코딩)
kubectl get secret <secret-name> -n <namespace> -o json | jq '.data | to_entries | map({key: .key, value: (.value | @base64d)})'

# 8. Service 확인
kubectl get svc -n <namespace>
kubectl describe svc <mysql-service-name> -n <namespace>

# 9. Endpoint 확인
kubectl get endpoints -n <namespace>

# 10. 네트워크 정책 확인
kubectl get networkpolicy -n <namespace>
```

### 9. 수정된 코드의 개선 사항

#### AuthService 개선

```java
// 데이터베이스 연결 오류를 명확히 구분
try {
    userOpt = userRepository.findByLoginId(request.getUserId());
} catch (DataAccessException e) {
    // DB 연결 오류 → 명확한 에러 메시지
    log.error("Database access error during login", e);
    throw new RuntimeException("데이터베이스 연결에 실패했습니다.", e);
}

if (userOpt.isEmpty()) {
    // 사용자 없음 → 인증 실패 메시지
    throw new RuntimeException("학번/사번 또는 비밀번호가 올바르지 않습니다.");
}
```

**이제 구분 가능:**
- 데이터베이스 연결 실패 → "데이터베이스 연결에 실패했습니다."
- 사용자 없음 → "학번/사번 또는 비밀번호가 올바르지 않습니다."
- 비밀번호 불일치 → "학번/사번 또는 비밀번호가 올바르지 않습니다."

### 10. 쿠버네티스 배포 예시

#### 전체 Deployment 예시

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grade-inquiry
  namespace: default
spec:
  replicas: 2
  selector:
    matchLabels:
      app: grade-inquiry
  template:
    metadata:
      labels:
        app: grade-inquiry
    spec:
      containers:
      - name: grade-inquiry
        image: your-registry/grade-inquiry:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_URL
          valueFrom:
            configMapKeyRef:
              name: grade-inquiry-config
              key: DB_URL
        - name: DB_USERNAME
          valueFrom:
            configMapKeyRef:
              name: grade-inquiry-config
              key: DB_USERNAME
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: grade-inquiry-secret
              key: DB_PASSWORD
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: grade-inquiry-service
  namespace: default
spec:
  selector:
    app: grade-inquiry
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

## 문제 해결 순서

1. **로그 확인** - Pod 로그에서 "Database access error" 확인
2. **헬스 체크** - `/actuator/health`에서 DB 상태 확인
3. **환경 변수 확인** - Pod 내부에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 확인
4. **네트워크 확인** - Pod에서 MySQL Service로 연결 테스트
5. **ConfigMap/Secret 확인** - 설정이 올바른지 확인
6. **MySQL 서버 확인** - MySQL이 실행 중이고 접근 가능한지 확인

이 가이드를 따라 문제를 단계적으로 해결할 수 있습니다.
