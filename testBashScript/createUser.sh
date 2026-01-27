#!/usr/bin/env bash

echo "== Create user =="
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d @- <<EOF
  {
    "username": "$1",
    "email": "$2",
    "password": "$3"
  }
EOF
echo -e "\n----------------------\n"