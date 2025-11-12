#!/usr/bin/env bash

set -euo pipefail

USER_ID=${1:-1}
BASE_URL=${BASE_URL:-http://localhost:8080}

IMPORT_JSON_FILE=${IMPORT_JSON_FILE:-import.json}
IMPORT_CSV_FILE=${IMPORT_CSV_FILE:-import.csv}

function header() {
  echo "== $1 =="
}

header "JSON Import"
if [[ -f "${IMPORT_JSON_FILE}" ]]; then
  curl -X POST "${BASE_URL}/api/users/${USER_ID}/import/json" \
    -H "Content-Type: application/json" \
    --fail --silent --show-error \
    --data-binary "@${IMPORT_JSON_FILE}"
  echo "Imported from ${IMPORT_JSON_FILE}"
else
  echo "JSON Import skipped: ${IMPORT_JSON_FILE} not found"
fi
echo -e "\n----------------------\n"

header "CSV Import"
if [[ -f "${IMPORT_CSV_FILE}" ]]; then
  curl -X POST "${BASE_URL}/api/users/${USER_ID}/import/csv" \
    -H "Content-Type: text/csv" \
    --fail --silent --show-error \
    --data-binary "@${IMPORT_CSV_FILE}"
  echo "Imported from ${IMPORT_CSV_FILE}"
else
  echo "CSV Import skipped: ${IMPORT_CSV_FILE} not found"
fi
echo -e "\n----------------------\n"

read -p "Press enter to exit"