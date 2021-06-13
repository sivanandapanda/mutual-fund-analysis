#!/bin/bash

mvn clean install package 

cd worker
docker build -f src/main/docker/Dockerfile.jvm -t sivadocker17/worker .

cd ../web
docker build -f src/main/docker/Dockerfile.jvm -t sivadocker17/web .

cd ../frontend
docker build -f Dockerfile -t sivadocker17/frontend .
