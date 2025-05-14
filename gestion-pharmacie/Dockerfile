FROM eclipse-temurin:21-jdk-alpine


WORKDIR /app


COPY pom.xml .


COPY src ./src


RUN apk add --no-cache maven && \
    mvn clean package -DskipTests


EXPOSE 8080


CMD ["sh", "-c", "java -jar target/*.jar --spring.profiles.active=dev"] 