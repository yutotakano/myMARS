@echo off
setlocal

rem We use the value the JAVACMD environment variable, if defined
rem and then try JAVA_HOME
set "_JAVACMD=%JAVACMD%"
if "%_JAVACMD"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)
if "%_JAVACMD%"=="" set _JAVACMD=java

rem Parses x out of 1.x; for example 8 out of java version 1.8.0_xx
rem Otherwise, parses the major version; 9 out of java version 9-ea
set JAVA_VERSION=0
for /f "tokens=3" %%g in ('%_JAVACMD% -Xms32M -Xmx32M -version 2^>^&1 ^| findstr /i "version"') do (
  set JAVA_VERSION=%%g
)
set JAVA_VERSION=%JAVA_VERSION:"=%
for /f "delims=.-_ tokens=1-2" %%v in ("%JAVA_VERSION%") do (
  if /I "%%v" EQU "1" (
    set JAVA_VERSION=%%w
  ) else (
    set JAVA_VERSION=%%v
  )
)

if %JAVA_VERSION% GTR 9 (
    echo Compiling Mars with default javac...
    dir /s /B *.java > sources.txt
    javac -encoding utf8 @sources.txt
    jar cfm Mars.jar META-INF/MANIFEST.MF README.md LICENSE.md PseudoOps.txt Config.properties Syscall.properties Settings.properties MipsXRayOpcode.xml registerDatapath.xml controlDatapath.xml ALUcontrolDatapath.xml CreateMarsJar.bat CreateMarsJar.sh Mars.java Mars.class docs help images mars
) else (
    echo Could not find an appropriate Java installation
)

echo CreateMarsJar finished.
endlocal
