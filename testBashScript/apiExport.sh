#!/usr/bin/env bash

set -euo pipefail

USER_ID=${1:-1}
BASE_URL=${BASE_URL:-http://localhost:8080}

EXPORT_JSON_FILE=${EXPORT_JSON_FILE:-export.json}
EXPORT_CSV_FILE=${EXPORT_CSV_FILE:-export.csv}

function header() {
  echo "== $1 =="
}

header "JSON Export"
curl "${BASE_URL}/api/users/${USER_ID}/export/json" \
  -H "Accept: application/json" \
  --fail --silent --show-error \
  -o "${EXPORT_JSON_FILE}"
echo "Exported JSON to ${EXPORT_JSON_FILE}"
echo -e "\n----------------------\n"

header "CSV Export"
curl "${BASE_URL}/api/users/${USER_ID}/export/csv" \
  -H "Accept: text/csv" \
  --fail --silent --show-error \
  -o "${EXPORT_CSV_FILE}"
echo "Exported CSV to ${EXPORT_CSV_FILE}"
echo -e "\n----------------------\n"

read -p "Press enter to exit"