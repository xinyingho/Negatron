#!/bin/sh

java_err()
{
    echo "Negatron requires Oracle's Java or OpenJDK/OpenJFX 8u60+ to run"
    exit 1
}

JAVA=`which java`
[ -z "$JAVA" ] && java_err

JRE=`java -version 2>&1 | head -1 | awk '{print $1}'`
[ "$JRE" = "java" ] || [ "$JRE" = "openjdk" ] || java_err

$JAVA -Xms512m -Xmx2g -jar Negatron.jar