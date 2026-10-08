# Etapa 1: compilar con Java 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: ejecutar sin privilegios de root
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app --no-create-home app
COPY --from=build /app/target/*.jar app.jar
USER app
EXPOSE 8082
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8082} -jar app.jar"]
