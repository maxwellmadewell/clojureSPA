FROM openjdk:8-alpine

COPY target/uberjar/third.jar /third/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/third/app.jar"]
