FROM openjdk:25-ea
RUN microdnf install -y curl && microdnf clean all
COPY target/*.jar services.jar

EXPOSE 80 81
CMD ["java", "-jar", "services.jar"]