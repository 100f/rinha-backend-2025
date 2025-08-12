FROM ghcr.io/graalvm/native-image-community:21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -Pnative

FROM alpine:3.20 AS prod

WORKDIR /app

COPY --from=build /app/target/rinha-backend-2025 /app/rinha-backend-2025

EXPOSE 8054

ENTRYPOINT ["top", "-b"]