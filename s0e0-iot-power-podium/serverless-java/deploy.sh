#!/usr/bin/env bash

set -e

BUILD_TAG="s0e0-iot-power-podium-build"
JAR="build/distributions/serverless-java-all.jar"

if command -v java &> /dev/null
then
  # Echo to standard error since we need to use standard out to determine which command to run by other scripts
  >&2 echo "Java detected, building natively"
  time ./gradlew build
elif command -v docker &> /dev/null
then
  # Echo to standard error since we need to use standard out to determine which command to run by other scripts
  >&2 echo "Java not detected, using Docker"
  >&2 echo "If you have not tried this before it may take a minute or two to build the container, subsequent runs will be faster"
  time docker build -t $BUILD_TAG . -f Dockerfile.build
  mkdir -p build/distributions
  docker run --rm $BUILD_TAG cat /serverless-java/$JAR > $JAR
fi

if [ ! -f "$JAR" ];
then
  >&2 echo "Neither Java nor Docker was detected. At least one of these needs to be present to run this code"
  exit 1
fi

./run-sls.sh deploy
