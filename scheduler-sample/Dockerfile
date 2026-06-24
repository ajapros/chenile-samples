FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/scheduler-sample-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
