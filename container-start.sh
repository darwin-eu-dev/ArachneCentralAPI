#!/bin/bash

# Start solr
solr start -cloud -f & 

# Start the second process
java -jar /app/portal.jar &

# Wait for any process to exit
wait -n

# Exit with status of process that exited first
exit $?
