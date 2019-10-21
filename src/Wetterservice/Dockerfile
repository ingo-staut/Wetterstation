#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
MAINTAINER Marius Schreck & Ingo Staut

#RUN apt-get update
#RUN apt-get install -y maven
COPY pom.xml /usr/local/service/
COPY src /usr/local/service/src
COPY data /usr/local/service/data
WORKDIR /usr/local/service/

RUN mvn clean
RUN mvn package



#
# Package stage
#

CMD ["java","-jar","target/Wetterservice-1.0-SNAPSHOT.jar"]
