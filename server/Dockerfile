FROM amazoncorretto:11
ENV TZ=Asia/Yekaterinburg
COPY target/*.jar server.jar
ENTRYPOINT ["java", "-jar", "/server.jar"]
