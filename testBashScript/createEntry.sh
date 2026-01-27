#!/usr/bin/env bash

echo "== Create user =="
curl -X POST http://localhost:8080/api/users/2/entries \
  -H "Content-Type: application/json" \
  -d @- <<EOF
  {
    "title": "Mein Titel",
    "content": "Mein Inhalt",
    "moodRating": 4
  }
EOF
echo -e "\n----------------------\n"