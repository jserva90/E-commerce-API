#!/bin/bash

if [ -z "$1" ]
then
  echo "No port number provided. Using default port 8080."
  APP_PORT=8080
else
  APP_PORT=$1
  echo "Starting application on port ${APP_PORT}"
fi

export APP_PORT

docker-compose up --build -d