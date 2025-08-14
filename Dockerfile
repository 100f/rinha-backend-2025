FROM ghcr.io/graalvm/native-image-community:21 AS build

RUN microdnf install maven -y && \
    mkdir -p /root/.m2

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -Pnative

COPY src ./src
RUN mvn clean package -Pnative

FROM alpine:3.20 AS prod

RUN apk add --no-cache libstdc++ gcompat

WORKDIR /app

COPY --from=build /app/target/rinha-backend-2025 /app/rinha-backend-2025
RUN chmod +x /app/rinha-backend-2025

EXPOSE 8054

ENTRYPOINT ["/app/rinha-backend-2025"]