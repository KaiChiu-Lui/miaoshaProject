FROM openjdk:8-jdk-alpine
COPY ../target/gatewayserver.jar /app/gatewayserver.jar
COPY ../src/main/resources/application.yml /app/application.yml

ENV SPRING_CONFIG_LOCATION=/app/application.yml

ENTRYPOINT ["java", "-jar", "/app/gatewayserver.jar"]