FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app
RUN groupadd --system spring \
    && useradd --system --gid spring --home-dir /app spring \
    && mkdir -p /app/uploads \
    && chown -R spring:spring /app
COPY --from=build --chown=spring:spring /build/target/campus-forum-1.0.0.jar /app/app.jar

USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
