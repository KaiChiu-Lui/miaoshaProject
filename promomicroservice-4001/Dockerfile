FROM openjdk:8-jdk-alpine
COPY ../target/promomicroservice.jar /app/promomicroservice.jar
COPY ../src/main/resources/application.properties /app/application.properties
COPY ../src/main/resources/logback-spring.xml /app/logback-spring.xml

ENV SPRING_CONFIG_LOCATION=/app/application.properties
ENV LOG_CONFIG_PATH=/app/logback-spring.xml

ENTRYPOINT ["java", "-jar", "/app/promomicroservice.jar"]