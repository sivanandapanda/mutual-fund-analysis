#/bin/bash

docker run --ulimit memlock=-1:-1 -d -it --rm=true --memory-swappiness=0 --name redis_quarkus_test -p 6379:6379 redis:5.0.6

docker run --rm=true --name worker -p 9001:9001 sivadocker17/worker

docker run --rm=true --name worker -p 8090:8080 sivadocker17/web

docker run --rm=true --name frontend -p 80:80 sivadocker17/frontend
