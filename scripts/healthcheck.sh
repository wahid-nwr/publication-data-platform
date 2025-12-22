#!/bin/bash
URL="http://localhost:8081/actuator/health"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" $URL)

if [ "$STATUS" -eq 200 ]; then
  echo "✅ App is healthy!"
else
  echo "❌ App is not responding (HTTP $STATUS)"
  exit 1
fi
