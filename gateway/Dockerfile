FROM amazoncorretto:11
ENV TZ=Asia/Yekaterinburg
COPY target/*.jar gateway.jar
ENTRYPOINT ["java", "-jar", "/gateway.jar"]
