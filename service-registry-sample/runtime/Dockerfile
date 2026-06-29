FROM maven:3.9.11-eclipse-temurin-25 AS build

ARG MODULE
ARG MODULE_PATH=chenile-samples/service-registry-sample/${MODULE}

WORKDIR /workspace
COPY . .
RUN mvn -q -f chenile-parent/pom.xml -DskipTests install
RUN mvn -q -f chenile-core/pom.xml -DskipTests install
RUN mvn -q -f chenile-service-registry/pom.xml -DskipTests install
RUN mvn -q -f chenile-samples/service-registry-sample/pom.xml -pl ${MODULE} -am -DskipTests package
RUN cp "$(find ${MODULE_PATH}/target -maxdepth 1 -name '*.jar' ! -name '*.original' | head -n 1)" /workspace/app.jar

FROM eclipse-temurin:25-jre

WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
