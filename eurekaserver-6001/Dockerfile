FROM openjdk:8-jdk-alpine
COPY ../target/eurekaserver-6001-1.0-SNAPSHOT.jar /app/eurekaserver.jar
COPY ../src/main/resources/application.yml /app/application.yml

ENV SPRING_CONFIG_LOCATION=/app/application.yml
ENV LOG_CONFIG_PATH=/app/logback-spring.xml

ENTRYPOINT ["java", "-jar", "/app/eurekaserver.jar"]