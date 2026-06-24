FROM eclipse-temurin:25-jre
WORKDIR /worker
COPY run.sh /worker/run.sh
RUN chmod +x /worker/run.sh
ENTRYPOINT ["/worker/run.sh"]
