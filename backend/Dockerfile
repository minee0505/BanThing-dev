# 1. 빌드 환경: Java 21 JDK
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# 2. 모든 소스 코드와 그래들 관련 파일 통째로 복사
COPY . .

# 3. 앱 빌드 (의존성 다운로드 + bootJar 한 번에)
RUN ./gradlew clean bootJar --no-daemon

# 4. 실행 환경: 가벼운 Java 21 JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 5. 빌드된 .jar 파일만 복사
COPY --from=builder /app/build/libs/app.jar app.jar

# 6. Render가 사용할 포트 설정
EXPOSE 9000

# 7. 앱 실행
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]