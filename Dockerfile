FROM openjdk:11-jdk
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar","--spring.config.name=application-prod"]
