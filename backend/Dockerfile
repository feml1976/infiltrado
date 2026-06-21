# ── Etapa 1: compilación ───────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Descarga dependencias antes de copiar el código fuente (cache de capas).
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn package -DskipTests -q

# ── Etapa 2: imagen de ejecución ───────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Usuario sin privilegios para ejecutar la aplicación.
RUN addgroup -S infiltrado && adduser -S infiltrado -G infiltrado
USER infiltrado

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8093

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-jar", "app.jar"]
