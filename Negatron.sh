#!/bin/bash

DIR=`dirname $0`
MINIMUMVERSION=11
JVM_OPTIONS="-Xms512m -Xmx2g --add-exports javafx.controls/com.sun.javafx.scene.control.behavior=negatron --add-opens javafx.controls/javafx.scene.control=negatron --add-opens javafx.controls/javafx.scene.control.skin=negatron --enable-native-access=negatron --module-path=\"$DIR/modules:$DIR/modules/lnx\""

# Check whether the system-wide Java runtime meets the minimum requirements

java_err() {
    read -n1 -r -p "**Negatron requires Oracle's Java or OpenJDK $MINIMUMVERSION+ to run**"
    exit 1
}

# default java check
JAVA=`which java`
[ -z "$JAVA" ] && java_err
# runtime check
JRE=`java -version 2>&1 | head -1 | awk '{print $1}'`
[ "$JRE" = "java" ] || [ "$JRE" = "openjdk" ] || java_err
# version check
MAJORVERSION=`java -version 2>&1 | head -1 | awk '{print substr($3, 2, length($3) - 2)}' | awk -F- '{print $1}' | awk -F. '{print $1}'`
[ "$MAJORVERSION" -ge "$MINIMUMVERSION" ] || java_err

# All checks have been passed
eval $JAVA $JVM_OPTIONS -m negatron/net.babelsoft.negatron.NegatronApp $@ &
