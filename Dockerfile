FROM openjdk:17-jdk

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

RUN ls -l /app

ENTRYPOINT ["java", "-jar", "/app/app.jar"]