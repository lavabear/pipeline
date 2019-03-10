# Thanks to http://whitfin.io/speeding-up-maven-docker-builds/
FROM maven:3.6.0-jdk-11 as maven

# copy the project files
COPY ./pom.xml ./pom.xml
COPY ./pipeline-core/pom.xml ./pipeline-core/pom.xml
COPY ./pipeline-db/pom.xml ./pipeline-db/pom.xml
COPY ./pipeline-ws/pom.xml ./pipeline-ws/pom.xml

COPY ./pipeline-core/src ./pipeline-core/src
COPY ./pipeline-db/src ./pipeline-db/src

RUN mvn install -pl pipeline-core,pipeline-db

# build all dependencies
RUN mvn dependency:go-offline -B

# copy your other files
COPY ./pipeline-ws/src ./pipeline-ws/src

# build for release
RUN mvn package

# our final base image
FROM openjdk:11.0.2-jre-stretch

# set deployment directory
WORKDIR /pipeline

# copy over the built artifact from the maven image
COPY --from=maven pipeline-ws/target/pipeline-ws-*jar-with-dependencies.jar ./pipeline.jar

# set the startup command to run your binary
CMD ["java", "-jar", "pipeline.jar"]