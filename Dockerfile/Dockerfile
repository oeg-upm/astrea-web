FROM eclipse-temurin:11

RUN mkdir app
WORKDIR /app
COPY ./astrea-service-2.2.1.jar ./astrea-service-2.2.1.jar


EXPOSE 8080

CMD ["java", "-jar", "./astrea-service-2.2.1.jar"]