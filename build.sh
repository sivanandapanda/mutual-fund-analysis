#!/bin/bash

profile="${1:-jvm}"

mvn clean install -DskipTests

mvn -f ui/pom.xml vaadin:prepare-frontend
mvn -f ui/pom.xml clean package -Pproduction spring-boot:repackage

mvn -f worker/pom.xml clean package

if [ "$profile" == "jvm" ]; then
  mvn -f worker/pom.xml clean package
  mvn -f web/pom.xml clean package
else 
  mvn -f worker/pom.xml package -Pnative -Dquarkus.native.container-build=true
  mvn -f web/pom.xml package -Pnative -Dquarkus.native.container-build=true
fi


docker build -t sivadocker17/mf-analysis-ui ui/.

if [ "$profile" == "jvm" ]; then
  docker build -f worker/src/main/docker/Dockerfile.jvm -t sivadocker17/mf-analysis-worker worker/.
  docker build -f web/src/main/docker/Dockerfile.jvm -t sivadocker17/mf-analysis-web web/.
else
  docker build -f worker/src/main/docker/Dockerfile.native -t sivadocker17/mf-analysis-worker worker/.
  docker build -f web/src/main/docker/Dockerfile.native -t sivadocker17/mf-analysis-web web/.
fi

docker image ls | head -4
