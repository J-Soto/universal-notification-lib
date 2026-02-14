# ==============================================================
# Universal Notification Lib -- Dockerfile
# Multi-stage build con Maven 3.9 + Eclipse Temurin JDK 21 (Alpine)
# ==============================================================

# --------------------------------------------------------------
# Etapa 1: Build
# --------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar solo el POM primero (aprovecha la cache de capas de Docker)
COPY pom.xml ./

# Descargar dependencias (capa cacheada si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Copiar el codigo fuente
COPY src/ src/

# Compilar y empaquetar (skip tests porque ya pasaron en CI)
RUN mvn clean package -DskipTests -B

# --------------------------------------------------------------
# Etapa 2: Runtime
# --------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL maintainer="NovaCom <dev@novacomp.com>"
LABEL description="Universal Notification Lib -- Demo con Virtual Threads (Java 21)"

WORKDIR /app

# Copiar el JAR y sus dependencias desde la etapa de build
COPY --from=build /app/target/universal-notification-lib-1.0.0.jar app.jar
COPY --from=build /app/target/libs/ libs/

# Ejecutar la demo
CMD ["java", "-jar", "app.jar"]
