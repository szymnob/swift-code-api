FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src
COPY src/main/resources/swiftCodes.xlsx ./src/main/resources/

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/src/main/resources/swiftCodes.xlsx /data/swiftCodes.xlsx


ENTRYPOINT ["java", "-jar", "/app/app.jar"]
