# FIRST WE DOWNLOAD AND BUILD THE UI
# NODE 16+ VERSIONS NOT SUPPORTED
FROM node:14.21-buster AS UI-BUILDER

# INSTALL DEPS
WORKDIR /arachne

RUN git clone https://github.com/darwin-eu-dev/ArachneUI.git

WORKDIR ./ArachneUI

RUN npm install
RUN npm run build


FROM maven:3.9.3-eclipse-temurin-17-alpine AS BUILDER

WORKDIR /tmp/maven

# THIS HAS BEEN WHEN THE ODYSSEUS NEXUS WAS NOT AVAILABLE AND THE BUILD IN THE AZURE PIPELINE WAS FAILING
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
COPY --from=UI-BUILDER /arachne/ArachneUI/public ./src/main/resources/public
RUN mvn package -DskipTests -DskipDocker -P dev

# WE ARE TAKING SOLR AS BASE IMAGE, IT IS BUILT ON TOP OF JAVA ADOPTIUM
FROM solr:8

WORKDIR /solr_config
COPY ./solr_config/conf /solr_config
RUN solr start -c && \
    solr create_collection -c users -n arachne-config && \
    solr create_collection -c data-sources -n arachne-config && \
    solr create_collection -c studies -n arachne-config && \
    solr create_collection -c analyses -n arachne-config && \
    solr create_collection -c analysis-files -n arachne-config && \
    solr create_collection -c papers -n arachne-config && \
    solr create_collection -c paper-protocols -n arachne-config && \
    solr create_collection -c paper-files -n arachne-config && \
    solr create_collection -c submissions -n arachne-config && \
    solr create_collection -c insights -n arachne-config && \
    solr create_collection -c result-files -n arachne-config && \
    solr create_collection -c study-files -n arachne-config && \
    solr zk upconfig -n arachne-config -d /solr_config -z localhost:9983 && \
    solr stop -all

USER root

WORKDIR /app

# LIBRE OFFICE IS USED FOR SOME PDF FUNCTIONALITY, APP DOES NOT RUN WITHOUT IT
RUN apt-get update && apt-get install -y --no-install-recommends \
    libreoffice libreoffice-writer && \
    apt-get -y autoremove && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /home/files/jcr/workspaces && chmod -R 777 /home/files/jcr

RUN chmod -R 777 /app

USER $SOLR_UID
COPY --from=BUILDER /tmp/maven/target/portal-exec.jar ./portal.jar
COPY container-start.sh ./container-start.sh

EXPOSE 8443

ENTRYPOINT ["./container-start.sh"]
