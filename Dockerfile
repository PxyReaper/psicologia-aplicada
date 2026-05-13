# Stage 1: Build con GraalVM Native Image
FROM gradle:jdk-21-and-23-graal-jammy as builder


WORKDIR /app

COPY . .
RUN chmod +x mvnw

RUN ./mvnw native:compile -Pnative

# Stage 2: Runtime (imagen ligera para binario nativo)
FROM debian:bookworm-slim

WORKDIR /app

# Copia el ejecutable nativo (sin extensión .jar)
COPY --from=builder /app/target/psicologia-aplicada /app/
COPY psicologiaaplicada-495911-e202675ec73d.json /app/

EXPOSE 8080

CMD ["/app/psicologia-aplicada"]
