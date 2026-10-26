# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY customer-service/pom.xml customer-service/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY account-service/src account-service/src

RUN mvn -pl account-service -am package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S treasury && adduser -S treasury -G treasury

COPY --from=build --chown=treasury:treasury /workspace/account-service/target/account-service-*.jar app.jar

USER treasury:treasury

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
