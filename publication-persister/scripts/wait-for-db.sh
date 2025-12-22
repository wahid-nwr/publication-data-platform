#!/bin/sh
set -e

#HOST="$1"
#PORT="$2"
#shift 2

MAX_RETRIES=60
SLEEP_INTERVAL=2

echo "🔍 Waiting for network to initialize before checking DB..."
sleep 3

echo "🔍 Waiting for database at $DB_HOST:$DB_PORT..."

i=1
while ! (nc -z -w1 "$DB_HOST" "$DB_PORT" </dev/null >/dev/null 2>&1); do
  if [ $i -ge $MAX_RETRIES ]; then
    echo "❌ Database not available after $MAX_RETRIES attempts — giving up."
    exit 1
  fi
  echo "⏳ Attempt $i/$MAX_RETRIES: database not ready, retrying in ${SLEEP_INTERVAL}s..."
  i=$((i+1))
  sleep $SLEEP_INTERVAL
done

echo "Database is up - executing command"
exec java \
  -Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
  -Dotel.logs.exporter=none -Dotel.metrics.exporter=none -Dotel.traces.exporter=none \
  -cp "/app/app.jar:/app/lib/*" \
  io.wahid.publication.PublicationManagementApplication