FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Replace this jar name if you change the artifact/version
ARG JAR_FILE=target/billing-backend-0.0.1-SNAPSHOT.jar

# Copy the built jar into the image
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
