FROM openjdk:8-alpine

ENV JAVA_OPTS="-server -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled"
ENV TZ Europe/Warsaw

COPY build/libs/logfile-reader.jar logfile-reader.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-XX:+UnlockExperimentalVMOptions","-jar","logfile-reader.jar"]
