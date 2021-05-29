#!/bin/bash

docker run --ulimit memlock=-1:-1 -d -it --rm=true --memory-swappiness=0 --name redis_quarkus_test -p 6379:6379 redis:5.0.6

