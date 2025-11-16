# 1. 빌드 환경: Java 21 JDK
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# 2. 그래들 파일 복사 (캐시 활용)
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# 3. 의존성 다운로드
RUN ./gradlew dependencies

# 4. 소스 코드 복사
COPY src ./src

# 5. 앱 빌드 (Dockerfile이 이 명령어를 실행함)
RUN ./gradlew clean bootJar

# 6. 실행 환경: 가벼운 Java 21 JRE
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# 7. 빌드된 .jar 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 8. Render가 사용할 포트 설정 (9000번)
EXPOSE 9000

# 9. 앱 실행 (Dockerfile이 이 명령어를 실행함)
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
CMD ["app.jar"]