#!/bin/bash

mvn clean install -DskipTests

mvn -f ui/pom.xml vaadin:prepare-frontend
mvn -f ui/pom.xml clean package -Pproduction spring-boot:repackage

#mvn -f worker/pom.xml clean package
mvn -f worker/pom.xml package -Pnative -Dquarkus.native.container-build=true

#mvn -f web/pom.xml clean package
mvn -f web/pom.xml package -Pnative -Dquarkus.native.container-build=true

docker build -t sivadocker17/mf-analysis-ui ui/.

#docker build -f server/src/main/docker/Dockerfile.jvm -t sivadocker17/fibonacci-server server/.
docker build -f worker/src/main/docker/Dockerfile.native -t sivadocker17/mf-analysis-worker-native worker/.

#docker build -f server/src/main/docker/Dockerfile.jvm -t sivadocker17/fibonacci-server server/.
docker build -f web/src/main/docker/Dockerfile.native -t sivadocker17/mf-analysis-web-native web/.

docker image ls | head -3