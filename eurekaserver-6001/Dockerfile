FROM openjdk:8-jdk-alpine
COPY ../target/eurekaserver.jar /app/eurekaserver.jar
COPY ../src/main/resources/application.yml /app/application.yml

ENV SPRING_CONFIG_LOCATION=/app/application.yml
ENV LOG_CONFIG_PATH=/app/logback-spring.xml

ENTRYPOINT ["java", "-jar", "/app/eurekaserver.jar"]