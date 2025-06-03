FROM eclipse-temurin:21 AS build

WORKDIR /app

COPY gradle/ gradle/
COPY gradlew .
COPY gradlew.bat .
COPY build.gradle .
COPY settings.gradle .

COPY src/ src/

RUN chmod +x gradlew
RUN ./gradlew build -x test

FROM eclipse-temurin:21

COPY --from=build /app/build/libs/blog-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]