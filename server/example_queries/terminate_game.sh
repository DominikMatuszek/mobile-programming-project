#!/bin/bash

curl -X 'POST' \
  'http://soturi.online:8000/getwinner' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xd",
  "password": "xd"
}'

curl -X 'POST' \
  'http://soturi.online:8000/getwinner' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xdd",
  "password": "xdd"
}'