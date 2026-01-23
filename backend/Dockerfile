# 1. 빌드 단계
FROM gradle:7.6-jdk17 AS builder
WORKDIR /app

# 소스 코드 복사
COPY . .

# 중요: gradlew 대신 그냥 'gradle' 명령어를 씁니다!
# (이미지 안에 gradle이 이미 설치되어 있어서 wrapper가 필요 없습니다)
RUN gradle clean build -x test --no-daemon

# 2. 실행 단계
FROM amazoncorretto:17
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경 변수 기본값 설정 (런타임에 오버라이드 가능)
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=jdbc:mysql://192.168.30.6:3306/univ_db?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
ENV DB_USERNAME=taekjunnn
# DB_PASSWORD는 보안상 런타임에 주입 (docker-compose.yml 또는 쿠버네티스에서 설정)

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
