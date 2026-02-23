@echo off
REM Script pour compiler, packager et deployer Ash_Conversion sur Tomcat 10
REM Usage: run.bat [clean|build|deploy|all]

setlocal enabledelayedexpansion

set PROJECT_NAME=Ash_Conversion
set WAR_FILE=target\%PROJECT_NAME%.war
set TOMCAT_HOME=%CATALINA_HOME%

REM Vérifier si Java est installé
where java >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Java n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Java JDK 24 et l'ajouter au PATH.
    exit /b 1
)

REM Vérifier si Maven est installé
where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERREUR] Maven n'est pas installe ou n'est pas dans le PATH.
    echo Veuillez installer Maven 3.9+ et l'ajouter au PATH.
    exit /b 1
)

REM Vérifier si CATALINA_HOME est défini
if "%TOMCAT_HOME%"=="" (
    echo [ERREUR] La variable d'environnement CATALINA_HOME n'est pas definie.
    echo Veuillez definir CATALINA_HOME avec le chemin vers votre installation Tomcat 10.
    echo Exemple: set CATALINA_HOME=C:\apache-tomcat-10.1.0
    exit /b 1
)

REM Vérifier si Tomcat existe
if not exist "%TOMCAT_HOME%\bin\catalina.bat" (
    echo [ERREUR] Tomcat introuvable dans: %TOMCAT_HOME%
    exit /b 1
)

set ACTION=%1
if "%ACTION%"=="" set ACTION=all

echo ========================================
echo Ash_Conversion - Script de deploiement
echo ========================================
echo.

if "%ACTION%"=="clean" goto :clean
if "%ACTION%"=="build" goto :build
if "%ACTION%"=="deploy" goto :deploy
if "%ACTION%"=="all" goto :all
goto :usage

:clean
echo [1/3] Nettoyage...
call mvn clean
if errorlevel 1 (
    echo [ERREUR] Echec du nettoyage
    exit /b 1
)
echo [OK] Nettoyage reussi
exit /b 0

:build
echo [2/3] Compilation et packaging...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation
    exit /b 1
)
if not exist "%WAR_FILE%" (
    echo [ERREUR] Le fichier WAR n'a pas ete cree: %WAR_FILE%
    exit /b 1
)
echo [OK] WAR cree avec succes: %WAR_FILE%
exit /b 0

:deploy
echo [3/3] Deploiement sur Tomcat...
if not exist "%WAR_FILE%" (
    echo [ERREUR] Le fichier WAR n'existe pas: %WAR_FILE%
    echo Veuillez d'abord executer: run.bat build
    exit /b 1
)

REM Arrêter Tomcat s'il est en cours d'exécution
echo Arret de Tomcat (si en cours d'execution)...
call "%TOMCAT_HOME%\bin\shutdown.bat" >nul 2>&1
timeout /t 3 /nobreak >nul

REM Vérifier si le port 8080 est toujours utilisé
netstat -an | findstr :8080 >nul 2>&1
if not errorlevel 1 (
    echo [ATTENTION] Le port 8080 semble toujours utilise.
    echo Attente de 5 secondes supplementaires...
    timeout /t 5 /nobreak >nul
)

REM Supprimer l'ancienne application
if exist "%TOMCAT_HOME%\webapps\%PROJECT_NAME%" (
    echo Suppression de l'ancienne application...
    rmdir /s /q "%TOMCAT_HOME%\webapps\%PROJECT_NAME%"
)
if exist "%TOMCAT_HOME%\webapps\%PROJECT_NAME%.war" (
    del /q "%TOMCAT_HOME%\webapps\%PROJECT_NAME%.war"
)

REM Créer le répertoire data pour la base de données H2 (si nécessaire)
if not exist "data" (
    echo Creation du repertoire data pour la base de donnees...
    mkdir data
)

REM Copier le nouveau WAR
echo Copie du WAR vers Tomcat...
copy "%WAR_FILE%" "%TOMCAT_HOME%\webapps\%PROJECT_NAME%.war"
if errorlevel 1 (
    echo [ERREUR] Echec de la copie du WAR
    exit /b 1
)

echo [OK] Deploiement reussi
echo.
echo Demarrage de Tomcat...
REM Utiliser start /wait pour voir les logs, ou start pour une fenêtre séparée
start "" "%TOMCAT_HOME%\bin\startup.bat"
timeout /t 5 /nobreak >nul
echo.
echo [OK] Tomcat demarre...
echo.
echo L'application sera disponible dans quelques secondes a:
echo http://localhost:8080/%PROJECT_NAME%/
echo.
echo Note: La base de donnees H2 sera creee automatiquement dans le repertoire 'data'
echo.
exit /b 0

:all
call :clean
if errorlevel 1 (
    echo [ERREUR] Echec lors du nettoyage
    exit /b 1
)
call :build
if errorlevel 1 (
    echo [ERREUR] Echec lors de la compilation
    exit /b 1
)
call :deploy
if errorlevel 1 (
    echo [ERREUR] Echec lors du deploiement
    exit /b 1
)
echo.
echo ========================================
echo [SUCCES] Application deployee avec succes!
echo ========================================
echo.
echo Tomcat a ete demarre automatiquement.
echo.
echo Pour arreter Tomcat:
echo   %TOMCAT_HOME%\bin\shutdown.bat
echo.
echo Application disponible sur:
echo   http://localhost:8080/%PROJECT_NAME%/
echo.
echo Compte de test (cree automatiquement):
echo   Username: admin
echo   Password: admin123
echo.
echo Base de donnees H2:
echo   Fichier: data\Ash_Conversion.mv.db
echo   (Cree automatiquement au premier demarrage)
echo.
exit /b 0

:usage
echo Usage: run.bat [clean^|build^|deploy^|all]
echo.
echo Options:
echo   clean   - Nettoie le projet (mvn clean)
echo   build   - Compile et package le projet (mvn clean package)
echo   deploy  - Deploie le WAR sur Tomcat
echo   all     - Execute clean, build et deploy (par defaut)
echo.
echo Exemple:
echo   run.bat all
echo   run.bat build
echo   run.bat deploy
exit /b 1

:end
endlocal
exit /b 0

