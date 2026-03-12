# Étape 1 : Build avec Maven et JDK 17
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Run avec Tomcat et JDK 17
FROM tomcat:10.1-jdk17-temurin

# Installation des outils pour les variables d'environnement
RUN apt-get update && apt-get install -y gettext-base zip unzip && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/local/tomcat
RUN rm -rf webapps/*

# Copie du war
COPY --from=build /app/target/*.war webapps/ROOT.war

# Script pour injecter les variables Railway dans le persistence.xml
CMD unzip webapps/ROOT.war WEB-INF/classes/META-INF/persistence.xml -d /tmp && \
    envsubst < /tmp/WEB-INF/classes/META-INF/persistence.xml > /tmp/persistence.xml.tmp && \
    mv /tmp/persistence.xml.tmp /tmp/WEB-INF/classes/META-INF/persistence.xml && \
    zip -j webapps/ROOT.war /tmp/WEB-INF/classes/META-INF/persistence.xml && \
    catalina.sh run
