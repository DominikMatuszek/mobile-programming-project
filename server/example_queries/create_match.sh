#!/bin/bash

curl -X 'POST' \
  'http://soturi.online:8000/createlobby' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xd",
  "password": "xd"
}'

curl -X 'POST' \
  'http://soturi.online:8000/joinlobby' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xdd",
  "password": "xdd",
  "lobby_owner_username": "xd"
}'

curl -X 'POST' \
  'http://soturi.online:8000/startmatch' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xd",
  "password": "xd"
}'