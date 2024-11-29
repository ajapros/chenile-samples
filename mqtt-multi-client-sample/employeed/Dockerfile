FROM openjdk:17
VOLUME ["/tmp" , "/db"]
RUN mkdir /app
COPY employeed-package/target/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]