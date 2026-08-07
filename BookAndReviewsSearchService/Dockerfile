# Use an official Maven image to build the application
FROM maven:3.9-eclipse-temurin-17 AS build
# Set the working directory in the container
WORKDIR /workspace/app

# Copy the pom.xml and source code to the container
COPY . .
# Build the application and skip tests to speed up the build process
RUN mvn install -DskipTests

# Create a directory for the application dependencies and extract the built JAR file into it
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Use an official Eclipse Temurin image to run the application
FROM eclipse-temurin:17-jre-jammy
# Set the working directory in the container
VOLUME /tmp
# Copy the application dependencies and classes from the build stage to the runtime stage
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Expose the application port
EXPOSE 8080

# Set the entry point to run the application
# The classpath includes the application classes and all the dependencies in the lib directory
# The main class is specified to start the Spring Boot application
# The application will run when the container starts, and it will listen on port 9000 for incoming requests
ENTRYPOINT ["java", "-cp", "app:app/lib/*", "rs.ac.uns.acs.nais.ElasticSearchDatabaseService.ElasticSearchDatabaseServiceApplication"]
