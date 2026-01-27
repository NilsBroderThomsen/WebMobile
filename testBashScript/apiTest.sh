#!/usr/bin/env bash

echo "== Get all entries =="
curl -X GET http://localhost:8080/api/users/1/entries
echo -e "\n----------------------\n"

echo "== Create entry =="
curl -X POST http://localhost:8080/api/users/1/entries \
  -H "Content-Type: application/json" \
  -d '{
    "title": "API Test 1",
    "content": "Das ist ein Testeintrag",
    "moodRating": 5
  }'
echo -e "\n----------------------\n"

echo "== Create entry without mood =="
curl -X POST http://localhost:8080/api/users/1/entries \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Ohne Stimmung",
    "content": "Kein MoodRating gesetzt"
  }'
echo -e "\n----------------------\n"

echo "== Update entry =="
curl -X PUT http://localhost:8080/api/entries/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Aktualisierter Titel",
    "content": "Aktualisierter Inhalt",
    "moodRating": 7
  }'
echo -e "\n----------------------\n"

echo "== Try delete that entry =="
curl -X DELETE http://localhost:8080/api/entries/1
echo -e "\n----------------------\n"

read -p "Press enter to exit"