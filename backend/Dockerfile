### 1️⃣ build stage – compile JAR with Gradle
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . /app
RUN chmod +x gradlew
RUN ./gradlew --no-daemon dependencies
RUN ./gradlew bootJar --no-daemon

### 2️⃣ runtime stage – lean JRE image
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=prod
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]