FROM maven:3.9.9-amazoncorretto-21-debian AS build
RUN apt-get update && apt-get install -y --no-install-recommends \
    git
RUN git clone https://github.com/sagansfault/sf6j.git /usr/src/sf6j
RUN mvn -f /usr/src/sf6j/pom.xml clean package install
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM amazoncorretto:21
COPY --from=build /usr/src/app/target/jaseppi-0.1.0.jar /usr/app/jaseppi-0.1.0.jar
CMD ["java","-jar","/usr/app/jaseppi-0.1.0.jar"]