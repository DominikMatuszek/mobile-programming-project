#!/bin/bash

curl -X 'POST' \
  'http://soturi.online:8000/reportposition' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xdd",
  "password": "xdd",
  "lon": 19.94492696849495,
  "lat": 50.077469469404434
}

'

curl -X 'POST' \
  'http://soturi.online:8000/reportposition' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xdd",
  "password": "xdd",
  "lon": 19.933357948451192,
  "lat": 50.05297921835436
}

'

curl -X 'POST' \
  'http://soturi.online:8000/reportposition' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xdd",
  "password": "xdd",
  "lon": 19.808668596730975,
  "lat": 50.08102296159998
}

'

# Get the state, cause why not 

curl -X 'POST' \
  'http://soturi.online:8000/getmatchstate' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "username": "xd",
  "password": "xd"
}'
