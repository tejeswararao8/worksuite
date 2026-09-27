@REM Maven Wrapper Script for Windows
@ECHO OFF

IF "%JAVA_HOME%" == "" (
  SET JAVACMD=java
) ELSE (
  SET JAVACMD=%JAVA_HOME%\bin\java
)

SET MAVEN_PROJECTBASEDIR=%~dp0
IF "%MAVEN_PROJECTBASEDIR:~-1%" == "\" SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

SET MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties

IF NOT EXIST "%MAVEN_WRAPPER_JAR%" (
  ECHO Downloading Maven Wrapper JAR...
  powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%MAVEN_WRAPPER_JAR%'"
)

%JAVACMD% ^
  --add-opens java.base/java.lang=ALL-UNNAMED ^
  --add-opens java.base/java.util=ALL-UNNAMED ^
  --enable-native-access=ALL-UNNAMED ^
  -cp "%MAVEN_WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*
