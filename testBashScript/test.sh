#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080}
USER_ID=${USER_ID:-1}

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
IMPORT_JSON="$SCRIPT_DIR/import.json"
IMPORT_CSV="$SCRIPT_DIR/import.csv"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

request() {
  local method="$1"
  local url="$2"
  local data_file="${3:-}"
  local content_type="${4:-}"
  local response_file
  response_file=$(mktemp)

  local curl_args=("-s" "-o" "$response_file" "-w" "%{http_code}" "-X" "$method")
  if [[ -n "$content_type" ]]; then
    curl_args+=("-H" "Content-Type: $content_type")
  fi
  if [[ -n "$data_file" ]]; then
    curl_args+=("--data-binary" "@$data_file")
  fi

  local status
  status=$(curl "${curl_args[@]}" "$url")

  echo "[$method] $url -> HTTP $status"
  if [[ -s "$response_file" ]]; then
    cat "$response_file"
    echo
  fi

  echo "$status" >"$response_file.status"
  echo "$response_file"
}

require_command curl
require_command python3

if [[ ! -f "$IMPORT_JSON" ]]; then
  echo "Missing import JSON payload at $IMPORT_JSON" >&2
  exit 1
fi

if [[ ! -f "$IMPORT_CSV" ]]; then
  echo "Missing import CSV payload at $IMPORT_CSV" >&2
  exit 1
fi

echo "Using BASE_URL=$BASE_URL"

echo "\n== Fetch existing entries =="
request "GET" "$BASE_URL/api/users/$USER_ID/entries" >/dev/null

echo "\n== Create entry =="
create_payload=$(mktemp)
cat >"$create_payload" <<'JSON'
{
  "title": "API Test Entry",
  "content": "Created by api_default_user_test.sh",
  "moodRating": 7
}
JSON

create_response_file=$(request "POST" "$BASE_URL/api/users/$USER_ID/entries" "$create_payload" "application/json")
create_status=$(cat "$create_response_file.status")

if [[ "$create_status" != "201" ]]; then
  echo "Create entry failed with HTTP $create_status" >&2
  exit 1
fi

ENTRY_ID=$(python3 - <<PY
import json
with open("$create_response_file") as fh:
    payload = json.load(fh)
print(payload["id"])
PY
)

echo "Created entry id: $ENTRY_ID"

echo "\n== Fetch entry details =="
request "GET" "$BASE_URL/api/entries/$ENTRY_ID" >/dev/null

echo "\n== Update entry =="
update_payload=$(mktemp)
cat >"$update_payload" <<'JSON'
{
  "title": "API Test Entry (Updated)",
  "content": "Updated by api_default_user_test.sh",
  "moodRating": 8
}
JSON
request "PUT" "$BASE_URL/api/entries/$ENTRY_ID" "$update_payload" "application/json" >/dev/null

echo "\n== Export JSON =="
request "GET" "$BASE_URL/api/users/$USER_ID/export/json" >/dev/null

echo "\n== Export CSV =="
request "GET" "$BASE_URL/api/users/$USER_ID/export/csv" >/dev/null

echo "\n== Import JSON =="
request "POST" "$BASE_URL/api/users/$USER_ID/import/json" "$IMPORT_JSON" "application/json" >/dev/null

echo "\n== Import CSV =="
request "POST" "$BASE_URL/api/users/$USER_ID/import/csv" "$IMPORT_CSV" "text/csv" >/dev/null

echo "\n== Delete created entry =="
request "DELETE" "$BASE_URL/api/entries/$ENTRY_ID" >/dev/null

echo "\n== Done =="