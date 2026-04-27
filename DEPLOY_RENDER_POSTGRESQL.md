# Déploiement Render + PostgreSQL

## Fichiers modifiés

- `pom.xml` : suppression du connecteur MySQL et ajout du driver PostgreSQL.
- `src/main/resources/application.yaml` : remplacement de `MYSQLHOST/MYSQLPORT/...` par `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`.
- `Dockerfile` : build propre avec Java 21, WAR exécutable Spring Boot.
- `.dockerignore` : exclusion de `.git`, `target`, `uploads`, logs et ZIP.
- `render.yaml` : création automatique du service web et de la base PostgreSQL Render.

## Variables utilisées par l'application

Render doit fournir :

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

Ne garde aucune ancienne variable MySQL :

- `MYSQLHOST`
- `MYSQLPORT`
- `MYSQLDATABASE`
- `MYSQLUSER`
- `MYSQLPASSWORD`
- `SPRING_DATASOURCE_URL` si elle commence par `jdbc:mysql`

## Action obligatoire après upload sur GitHub

Sur Render :

1. Supprimer les anciennes variables MySQL.
2. Faire `Clear build cache & deploy`.
3. Vérifier dans les logs que `mysql-connector-j` n'apparaît plus.
