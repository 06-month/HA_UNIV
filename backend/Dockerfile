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

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
