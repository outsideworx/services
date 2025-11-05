FROM openjdk:25-ea
COPY target/*.jar vault.jar

EXPOSE 80 443
CMD ["java", "-jar", "vault.jar"]