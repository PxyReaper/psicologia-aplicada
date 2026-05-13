# Stage 1: Build con GraalVM Native Image
FROM gradle:jdk-21-and-23-graal-jammy AS builder

WORKDIR /app

# Cache Maven wrapper (solo se invalida si cambia mvnw o .mvn)
COPY mvnw ./
COPY .mvn/ ./.mvn/
RUN chmod +x mvnw && ./mvnw --version

# Cache dependencias de Maven (solo se invalida si cambia pom.xml)
COPY pom.xml ./
RUN ./mvnw -B -Pnative dependency:go-offline

# Compilar binario nativo (tests se ejecutan en CI, no en build de imagen)
COPY src ./src
RUN ./mvnw -B -Pnative -DskipTests native:compile

# Stage 2: Runtime (imagen ligera para binario nativo)
FROM debian:bookworm-slim

WORKDIR /app

COPY --from=builder /app/target/psicologia-aplicada /app/
COPY psicologiaaplicada-495911-e202675ec73d.json /app/

EXPOSE 8080

CMD ["/app/psicologia-aplicada"]
