# Étape 1 : Build
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Run
FROM tomcat:10.1-jdk21

# Installation des outils nécessaires (Correction du tiret ici)
RUN apt-get update && apt-get install -y gettext-base zip unzip && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/local/tomcat
RUN rm -rf webapps/*

# Copie du war
COPY --from=build /app/target/*.war webapps/ROOT.war

# Script pour injecter les variables dans le persistence.xml à l'intérieur du WAR
CMD unzip webapps/ROOT.war WEB-INF/classes/META-INF/persistence.xml -d /tmp && \
    envsubst < /tmp/WEB-INF/classes/META-INF/persistence.xml > /tmp/persistence.xml.tmp && \
    mv /tmp/persistence.xml.tmp /tmp/WEB-INF/classes/META-INF/persistence.xml && \
    zip -j webapps/ROOT.war /tmp/WEB-INF/classes/META-INF/persistence.xml && \
    catalina.sh run