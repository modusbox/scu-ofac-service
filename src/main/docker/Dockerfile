## Docker base image https://github.com/modusintegration/portx-payments-base/blob/main/springboot/2/jdk11/Dockerfile
FROM 854173534877.dkr.ecr.us-west-2.amazonaws.com/portx-payments/base-java:springboot-2-jdk11
USER root
VOLUME /tmp
ADD ./target/ofac-opensource*.jar app.jar
RUN bash -c 'touch /app.jar'
EXPOSE 8080
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]