#!/usr/bin/env bash

JAR="build/libs/hue-lifx-java-1.0-SNAPSHOT-all.jar"

# Echo to standard error since we need to use standard out to determine which command to run by other scripts
if command -v java &> /dev/null
then
  >&2 echo "Java detected, building natively"
  >&2 ./gradlew build
  echo "java -cp $JAR"
  exit 0
fi

# Echo to standard error since we need to use standard out to determine which command to run by other scripts
if command -v docker &> /dev/null
then
  >&2 echo "Java not detected, using Docker"
  >&2 echo "If you have not tried this before it may take a minute or two to build the container, subsequent runs will be faster"
  TAG="hue-lifx-java"
  >&2 docker build -t $TAG .
  echo "docker run --network=host --rm $TAG $JAR"
  exit 0
fi

>&2 echo "Neither Java nor Docker was detected. At least one of these needs to be present to run this code"
exit 1