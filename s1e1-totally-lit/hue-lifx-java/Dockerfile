FROM amazoncorretto:11-al2-full

# Make sure we use UTF-8 to avoid JSON output issues (without this special chars like non-stand
ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Get Gradle set up
COPY build.gradle /hue-lifx-java/
COPY gradlew /hue-lifx-java/
COPY gradle/wrapper/ /hue-lifx-java/gradle/wrapper

# Do the clean here so we don't download Gradle every time the source code changes
WORKDIR /hue-lifx-java
RUN ./gradlew clean

COPY src /hue-lifx-java/src

# Clean again just in case any junk crept into the src directory
RUN ./gradlew clean

# Build the JAR file
RUN ./gradlew build

ENTRYPOINT ["java", "-cp"]
