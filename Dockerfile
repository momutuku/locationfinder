FROM openjdk:18
WORKDIR /app
COPY ./target/geojson-0.0.1-SNAPSHOT.jar /app
EXPOSE 8080
ENV EXTRA_OPTS=""
CMD exec java -Xms1g -Xmx2g $EXTRA_OPTS -jar geojson-0.0.1-SNAPSHOT.jar
