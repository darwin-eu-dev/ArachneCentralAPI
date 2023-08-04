# CREATES A PRECONFIGURED SOLR IMAGE, TO AVOID INSTALLING AND CONFIGURING SOLR ON YOUR MACHINE
# BUILD WITH docker build -t arachne-solr .
# RUN WITH docker run -p 8983:8983 arachne-solr
FROM solr:8

WORKDIR /solr_config

COPY ./conf /solr_config

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

CMD ["solr","start","-cloud","-f"]
