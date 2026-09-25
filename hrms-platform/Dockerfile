FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S hrms && adduser -S hrms -G hrms
COPY --from=builder /app/target/*.jar app.jar
USER hrms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
