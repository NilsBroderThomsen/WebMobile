#!/usr/bin/env bash

API_URL="http://localhost:8080/api/users/2/entries"

if [ -z "$TOKEN" ]; then
  echo "TOKEN ist nicht gesetzt. Mach erstmal:"
  echo "export TOKEN=dein_token"
  exit 1
fi

titles=(
  "Montag halt"
  "Produktiver Tag"
  "Chaos im Kopf"
  "Ganz okay"
  "Zu wenig Schlaf"
  "Kaffee regelt"
  "Alles nervt"
  "Flow-Zustand"
  "Null Motivation"
  "Überraschend gut"
  "Brainfog deluxe"
  "Kleiner Sieg"
  "So lala"
  "Endlich Fortschritt"
  "Feierabend-Vibes"
)

contents=(
  "Der Start in die Woche war zäh."
  "Hab heute echt was geschafft."
  "Gedanken springen wie Flipperkugeln."
  "Nicht gut, nicht schlecht."
  "Schlafen wäre mal wieder cool gewesen."
  "Koffein hat schlimmeres verhindert."
  "Alles war anstrengend heute."
  "Stundenlang konzentriert gewesen."
  "Nichts ging voran."
  "Besser als erwartet."
  "Kopf fühlt sich an wie Watte."
  "Kleines Erfolgserlebnis."
  "Irgendwie durch den Tag gekommen."
  "Langsam fügt sich alles."
  "Arbeit done, Kopf aus."
)

moods=(3 6 4 5 2 6 3 8 2 7 4 6 5 7 8)

for i in {0..14}; do
  echo "== Erstelle Entry $((i+1)) =="

  curl -s -X POST "$API_URL" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d @- <<EOF
{
  "title": "${titles[$i]}",
  "content": "${contents[$i]}",
  "moodRating": ${moods[$i]}
}
EOF

  echo -e "\n----------------------\n"
  sleep 0.2
done

echo "15 Entries erstellt. Datenbank ist jetzt weniger leer. 🎉"
