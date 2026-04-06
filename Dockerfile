# --- Build Stage ---
FROM gradle:8.14-jdk24 AS build
WORKDIR /app
COPY . .
RUN gradle clean bootJar --no-daemon

# --- Run Stage ---
FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]