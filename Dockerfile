FROM maven:3.9-eclipse-temurin-21-alpine
WORKDIR /


FROM openjdk:21-jdk-alpine
WORKDIR /app


ENTRYPOINT ["top", "-b"]