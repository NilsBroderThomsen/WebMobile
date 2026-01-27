#!/usr/bin/env bash

echo "== login =="
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d @- <<'EOF'
{
  "username": "Nils",
  "password": "testtest"
}
EOF
