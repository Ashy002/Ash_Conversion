FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q dependency:go-offline -DskipTests

COPY src src
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/ash-conversion-0.0.1-SNAPSHOT.war app.war

ENV JAVA_OPTS="-Xmx256m -Xms128m"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war --server.servlet.register-default-servlet=true"]
