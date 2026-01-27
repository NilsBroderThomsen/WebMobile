#!/usr/bin/env bash

TOKEN="$1"

echo "== Get entries =="

curl -X GET http://localhost:8080/api/users/1/entries \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n----------------------\n"
