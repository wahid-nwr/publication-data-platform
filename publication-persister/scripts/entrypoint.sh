#!/bin/sh
set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
MAX_RETRIES=60
SLEEP_INTERVAL=2

echo "⏳ Waiting for Postgres at $DB_HOST:$DB_PORT ..."

i=1
while ! nc -z "$DB_HOST" "$DB_PORT" >/dev/null 2>&1; do
  if [ $i -ge $MAX_RETRIES ]; then
    echo "❌ Database not available after $MAX_RETRIES attempts — giving up."
    exit 1
  fi

  echo "   Attempt $i/$MAX_RETRIES: not ready yet..."
  i=$((i+1))
  sleep $SLEEP_INTERVAL
done

echo "✅ Database is up — starting application."

# Cloud Run provides port via $PORT, so your Jetty must bind to it.
JAVA_PORT=${PORT:-8080}

exec java \
  -Xms256m \
  -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dserver.port="$JAVA_PORT" \
  -cp "/app/app.jar:/app/lib/*" \
  io.wahid.publication.PublicationManagementApplication
