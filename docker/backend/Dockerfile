#Build stage
FROM maven:3.8.7-openjdk-18 AS build
WORKDIR /build
COPY msp_backend/pom.xml .
RUN mvn dependency:go-offline
COPY msp_backend/src ./src
COPY documentation /documentation
RUN mvn clean package -DskipTests

#Runtime stage
FROM amazoncorretto:17


#define few things
WORKDIR /app
COPY --from=build /build/target/msp_backend-*.jar /app/msp_backend.jar
COPY fichiers /app/fichiers
EXPOSE 8088

#CMD java -jar -Dspring.profiles.active=${ACTIVE_PROFILE} -Dspring.datasource.url=${DB_URL} msp_backend-${JAR_VERSION}.jar
CMD ["sh", "-c", "java -jar /app/msp_backend.jar"]
