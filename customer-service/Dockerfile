# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY customer-service/pom.xml customer-service/pom.xml
COPY account-service/pom.xml account-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY customer-service/src customer-service/src

RUN mvn -pl customer-service -am package -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S treasury && adduser -S treasury -G treasury

COPY --from=build --chown=treasury:treasury /workspace/customer-service/target/customer-service-*.jar app.jar

USER treasury:treasury

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
