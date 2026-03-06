# Étape 1 : Build avec Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Run avec Tomcat
FROM tomcat:10.1-jdk21-slim 
# Note : 'slim' est plus léger, ce qui économise tes crédits Railway !

RUN rm -rf /usr/local/tomcat/webapps/*

# On utilise un chemin plus précis pour éviter les conflits de fichiers
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

# Railway injecte parfois une variable PORT, Tomcat doit l'écouter
CMD ["catalina.sh", "run"]