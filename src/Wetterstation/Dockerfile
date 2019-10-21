#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
MAINTAINER Marius Schreck & Ingo Staut

#RUN apt-get update
#RUN apt-get install -y maven
COPY pom.xml /usr/local/service/
COPY src /usr/local/service/src/
COPY index.html /usr/local/service/
COPY 404.html /usr/local/service/
COPY returnJsonFile.json /usr/local/service/
COPY internSensorData.txt /usr/local/service/

WORKDIR /usr/local/service/

RUN mvn clean
RUN mvn package



#
# Package stage
#

CMD ["java","-jar","target/Wetterstation-1.0-SNAPSHOT.jar"]
