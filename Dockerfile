# ---- Build stage ----
FROM eclipse-temurin:25 AS build
WORKDIR /workspace

COPY gradlew build.gradle settings.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon testClasses || true

COPY src src
RUN ./gradlew --no-daemon test bootJar

# ---- Runtime stage ----
FROM eclipse-temurin:25
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]