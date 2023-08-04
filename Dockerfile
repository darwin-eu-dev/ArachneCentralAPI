FROM maven:3.8.7-eclipse-temurin-17-alpine as BUILDER

WORKDIR /tmp/maven

COPY lib ./lib
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/authenticator-0.0.3-QA.jar
RUN mvn "org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file" -Dfile=lib/SqlRender-1.12.0.jar

COPY pom.xml .
RUN mvn dependency:go-offline

COPY google_checkstyle.xml .
COPY ./src/main ./src/main
RUN mvn clean package -DskipTests -DskipDocker -P dev


FROM eclipse-temurin:8-jre-alpine

WORKDIR /app

RUN apk update && apk add libreoffice libreoffice-writer

RUN adduser -D service -S -g "First"
RUN chmod -R 777 /app
USER service
COPY --from=BUILDER /tmp/maven/target/portal-exec.jar ./portal.jar
ENTRYPOINT ["java", "-jar", "portal.jar"]
EXPOSE 8443
