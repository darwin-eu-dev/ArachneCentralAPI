FROM maven:3.9.3-eclipse-temurin-17-alpine AS BUILDER

WORKDIR /tmp/maven

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
