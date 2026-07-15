FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/bulk-upload-process-sample-*.jar /app/app.jar
EXPOSE 8095
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
