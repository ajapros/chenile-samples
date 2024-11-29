FROM openjdk:17
VOLUME ["/tmp" , "/db"]
RUN mkdir /app
COPY *-package/target/*.jar /app/app.jar
ENV store=store1
ENV port=8001
ENV tenant=t1
ENTRYPOINT ["java","-Dspring.profiles.active=store","-Dmqtt.clientID=${store}","-Dserver.port=${port}","-Dmqtt.subscribe.base.topic=c/${tenant}","-jar","/app/app.jar","--trace"]
