# Étape 1 : Build avec Maven et JDK 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Run avec Tomcat et JDK 17
FROM tomcat:10.1-jdk17-temurin

WORKDIR /usr/local/tomcat
RUN rm -rf webapps/*

# Copie du WAR généré
COPY --from=build /app/target/ROOT.war webapps/ROOT.war

# Démarrage Tomcat
CMD ["catalina.sh", "run"]
