@echo off
setlocal enabledelayedexpansion

:: Recherche automatique du dossier JDK
set "JAVA_HOME_PATH="
for /d %%i in ("C:\Program Files\Java\jdk-*") do (
    set "JAVA_HOME_PATH=%%i"
)

if "!JAVA_HOME_PATH!"=="" (
    echo [ERREUR] JDK non trouve dans C:\Program Files\Java\
    echo Veuillez verifier que vous avez installe le JDK.
    pause
    exit /b
)

set "JDK_BIN=!JAVA_HOME_PATH!\bin"
set "JAVAC=!JDK_BIN!\javac.exe"
set "JAR=!JDK_BIN!\jar.exe"

echo [1/5] Nettoyage...
if exist bin rd /s /q bin
mkdir bin

echo [2/5] Extraction des dependances Minim...
if not exist lib\minim_core.jar (
    echo [ERREUR] lib\minim_core.jar introuvable.
    pause
    exit /b
)
cd bin
"!JAR!" xf ..\lib\minim_core.jar
cd ..

echo [3/5] Compilation des sources Java...
"!JAVAC!" -d bin -cp "lib/*" src/autostepper/*.java

echo [4/5] Generation du Manifeste...
echo Manifest-Version: 1.0 > manifest.mf
echo Main-Class: autostepper.AutoStepper >> manifest.mf
echo Class-Path: lib/jl1.0.1.jar lib/jsoup-1.11.2.jar lib/mp3spi1.9.5.jar lib/tritonus_aos.jar lib/tritonus_share.jar lib/trove-3.0.3.jar lib/jopt-simple-5.0.4.jar lib/jsminim.jar >> manifest.mf
echo. >> manifest.mf

echo [5/5] Creation du JAR final (AutoStepper.jar)...
"!JAR!" cfm AutoStepper.jar manifest.mf -C bin .

echo.
echo [SUCCES] Votre fichier AutoStepper.jar a ete genere avec succes !
pause
