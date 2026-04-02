FROM openjdk:25-ea
COPY target/*.jar services.jar

EXPOSE 80 81
CMD ["java", "-jar", "services.jar"]