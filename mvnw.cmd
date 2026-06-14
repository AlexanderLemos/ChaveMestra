@echo off
setlocal

set "MAVEN_CMD=%TEMP%\maven\apache-maven-3.9.9\bin\mvn.cmd"

if not exist "%MAVEN_CMD%" (
    echo Maven not found. Downloading Maven 3.9.9...
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip' -OutFile '%TEMP%\maven-dist.zip' -UseBasicParsing; Expand-Archive -Path '%TEMP%\maven-dist.zip' -DestinationPath '%TEMP%\maven' -Force"
    if not exist "%MAVEN_CMD%" (
        echo ERROR: Maven download failed.
        exit /b 1
    )
    echo Maven installed successfully.
)

"%MAVEN_CMD%" %*

endlocal
