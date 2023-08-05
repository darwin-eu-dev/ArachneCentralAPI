FROM maven:3.9.3-eclipse-temurin-17-alpine AS BUILDER

WORKDIR /tmp/maven

COPY lib ./lib
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-common-types-1.19.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-common-utils-1.19.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-commons-1.19.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-no-handler-found-exception-util-1.19.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-scheduler-1.17.3.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-storage-1.19.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-sys-settings-1.19.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/data-source-manager-1.17.3.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/execution-engine-commons-1.19.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/logging-1.17.3.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/authenticator-0.0.3-QA.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/SqlRender-1.12.0.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/arachne-commons-bundle-1.19.0.pom -DgroupId=com.odysseusinc.arachne -DartifactId=arachne-commons-bundle -Dversion=1.19.0 -Dpackaging=pom


COPY pom.xml .
RUN mvn dependency:go-offline

COPY google_checkstyle.xml .
COPY ./src/main ./src/main
RUN mvn clean package -DskipTests -DskipDocker -P dev


FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

RUN apk update && apk add libreoffice libreoffice-writer
RUN mkdir -p /var/arachne/files/jcr/workspaces && chmod -R 777 /var/arachne/files/jcr

RUN adduser -D service -S -g "First"
RUN chmod -R 777 /app
USER service
COPY --from=BUILDER /tmp/maven/target/portal-exec.jar ./portal.jar
ENTRYPOINT ["java", "-jar", "portal.jar"]
EXPOSE 8443
