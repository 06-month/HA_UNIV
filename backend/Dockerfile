# 1. 빌드 단계
FROM gradle:7.6-jdk17 AS builder
WORKDIR /app

# Gradle 의존성 캐시를 위한 설정 파일 먼저 복사 (캐시 최적화)
COPY build.gradle settings.gradle ./
COPY gradle/ ./gradle/

# 의존성 다운로드 (소스 코드 변경 없이 의존성만 변경되면 이 레이어 재사용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src/ ./src/

# 빌드 실행
RUN gradle clean build -x test --no-daemon

# JAR 파일 존재 확인
RUN ls -la /app/build/libs/*.jar || (echo "JAR file not found!" && exit 1)

# 2. 실행 단계
FROM amazoncorretto:17
WORKDIR /app

# 빌드 결과물 복사 (plain JAR 제외, 실행 가능한 JAR만)
COPY --from=builder /app/build/libs/grade-inquiry-backend-*.jar app.jar

# 환경 변수 기본값 설정 (런타임에 오버라이드 가능)
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=jdbc:mysql://192.168.30.6:3306/univ_db?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
ENV DB_USERNAME=taekjunnn
# DB_PASSWORD는 보안상 런타임에 주입 (docker-compose.yml 또는 쿠버네티스에서 설정)

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
