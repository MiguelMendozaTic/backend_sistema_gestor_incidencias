# Etapa 1: Construcción (Build)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/sistema_gestor_incidencias-0.0.1-SNAPSHOT.jar app.jar

# Render usa la variable de entorno PORT, nos aseguramos de exponerla
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]