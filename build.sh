#!/bin/bash
set -e

# Find the jar tool: JAVA_HOME, then PATH, then the JDK that `java` runs from.
# (On Windows, the Oracle installer only puts java/javac shims on the PATH, not jar.)
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/jar" ]; then
    JAR="$JAVA_HOME/bin/jar"
elif command -v jar >/dev/null 2>&1; then
    JAR=jar
else
    JAVA_HOME_DIR=$(java -XshowSettings:properties -version 2>&1 | sed -n 's/^ *java.home = //p' | tr -d '\r')
    JAR="$JAVA_HOME_DIR/bin/jar"
fi

mkdir -p out dist
javac -d out $(find src -name '*.java')
"$JAR" cfe dist/mc-skin-changer.jar net.tjnewbry.skinhead.cli.Main -C out .
echo "Built dist/mc-skin-changer.jar"
