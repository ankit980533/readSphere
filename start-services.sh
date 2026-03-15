#!/bin/sh

# Start backend on port 8081
java -jar app.jar --server.port=8081 --spring.profiles.active=prod &

# Wait for backend to start
sleep 10

# Start nginx on port 8080
nginx -g 'daemon off;'
