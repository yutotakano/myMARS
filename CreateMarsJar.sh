#!/usr/bin/env bash

if type -p java; then
    echo "Found Java executable in PATH"
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    echo "Found Java executable in JAVA_HOME"
    _java="$JAVA_HOME/bin/java"
else
    echo "Could not find an installation of Java"
fi

if [[ "$_java" ]]; then
    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "version $version"

    IFS=. read major minor extra <<< "$version";

    if ((major > 9 || (major == 1 && minor > 9))); then
        echo "Compiling Mars with default javac"
        javac -cp ".:lib/*" $(find . -name "*.java")
    else
        echo "Attempting to compile Mars with java-10"
        find . -name "*.java" | xargs /usr/lib/jvm/java-10/bin/javac
    fi
fi

jar cfm Mars.jar META-INF/MANIFEST.MF README.md LICENSE.md PseudoOps.txt Config.properties Syscall.properties Settings.properties MipsXRayOpcode.xml registerDatapath.xml controlDatapath.xml ALUcontrolDatapath.xml CreateMarsJar.bat CreateMarsJar.sh Mars.java Mars.class docs help images mars lib

echo "CreateMarsJar finished"
