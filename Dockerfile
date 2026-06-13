FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY . .
RUN ./mvnw -q -DskipTests -pl ${SERVICE} -am package

FROM eclipse-temurin:25-jre
ARG SERVICE
ENV SERVICE_DIR=${SERVICE}
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
