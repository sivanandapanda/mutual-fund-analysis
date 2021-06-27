#!/bin/bash

docker run -d --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name redis_server -p 6379:6379 redis:5.0.6
