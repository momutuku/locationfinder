FROM tomcat:10.1.36-jdk17-temurin

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR
COPY target/geofinder.war /usr/local/tomcat/webapps/geofinder.war

# Copy custom context.xml to enable bigger cache
COPY context.xml /usr/local/tomcat/conf/context.xml

# Change Tomcat port to 8081
RUN sed -i 's/port="8080"/port="8081"/' /usr/local/tomcat/conf/server.xml

EXPOSE 8081

CMD ["catalina.sh", "run"]