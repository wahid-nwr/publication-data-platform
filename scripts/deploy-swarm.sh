#!/bin/bash
set -e

# === CONFIGURATION ===
STACK_NAME="publication"
NETWORK_NAME="publication-net"
STACK_FILE="/home/deploy/publication/stack.yml"
OBSERVABILITY_STACK_NAME="observability"
OBSERVABILITY_STACK_FILE="/home/deploy/publication/stack-observability.yml"

DB_CONTAINER="pubdb"
DB_IMAGE="postgres:16-alpine"
DB_USER="wahid"
DB_PASSWORD="anwar"
DB_NAME="publication-db"
DB_PORT=5432

echo "=== Deploying PublicationManagement stack on Docker Swarm ==="

#echo "🐳 Initializing Docker Swarm (if not already)..."
#docker swarm init 2>/dev/null || echo "Swarm already active"
# === 1. Ensure Docker Swarm is initialized ===
echo "🔍 Checking Docker Swarm status..."
if ! docker info | grep -q "Swarm: active"; then
  echo "🚀 Initializing Docker Swarm..."
  docker swarm init
else
  echo "✅ Swarm already active."
fi

# === 2. Ensure overlay network exists ===
echo "🌐 Checking overlay network '$NETWORK_NAME'..."
if ! docker network ls --format '{{.Name}}' | grep -q "^${NETWORK_NAME}\$"; then
  echo "🛠️  Creating overlay network..."
  docker network create --driver overlay --attachable "$NETWORK_NAME"
else
  echo "✅ Network '$NETWORK_NAME' already exists."
fi

# === 3. Ensure Postgres container is running ===
echo "🐘 Checking PostgreSQL container '$DB_CONTAINER'..."
if ! docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}\$"; then
  if docker ps -a --format '{{.Names}}' | grep -q "^${DB_CONTAINER}\$"; then
    echo "🧹 Removing old PostgreSQL container..."
    docker rm -f "$DB_CONTAINER" >/dev/null 2>&1 || true
  fi

  echo "📦 Starting PostgreSQL container..."
  docker run -d \
    --name "$DB_CONTAINER" \
    --network "$NETWORK_NAME" \
    -e POSTGRES_USER="$DB_USER" \
    -e POSTGRES_PASSWORD="$DB_PASSWORD" \
    -e POSTGRES_DB="$DB_NAME" \
    -p "$DB_PORT:$DB_PORT" \
    --memory=256m \
    "$DB_IMAGE" >/dev/null

  echo "⏳ Waiting for PostgreSQL to initialize..."
  sleep 5
else
  echo "✅ PostgreSQL container already running."
fi

# Wait until PostgreSQL responds on port 5432
echo "⏳ Waiting for PostgreSQL to accept connections..."
until docker exec "$DB_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1; do
  printf '.'
  sleep 1
done
echo ""
echo "✅ PostgreSQL is ready."

#STACK_NAME="publication"
#STACK_FILE="./../stack.yml"

# Pull latest image
echo "📦 Pulling latest DockerHub images..."
docker pull us-central1-docker.pkg.dev/alert-cursor-476219-s1/publication-repo/publication-common:latest
docker pull us-central1-docker.pkg.dev/alert-cursor-476219-s1/publication-repo/csv-producer:latest

# Deploy (update existing observability stack)
# Disabling for now
echo "✅ Deploying observability stack"
docker stack deploy -c $OBSERVABILITY_STACK_FILE $OBSERVABILITY_STACK_NAME
sleep 10

# Deploy (update existing stack)
echo "✅ Deploying publication stack"
docker stack deploy -c $STACK_FILE $STACK_NAME

# Clean old images
docker image prune -f

# === 5. Wait for stack startup ===
echo "⏳ Waiting for services to start..."
sleep 10

echo "✅ Deployment triggered successfully."

# === 6. Summary ===
echo ""
echo "✅ Deployment summary:"
echo "  - Stack name: $STACK_NAME"
echo "  - Network: $NETWORK_NAME (overlay)"
echo "  - Database: $DB_CONTAINER ($DB_IMAGE)"
echo "  - DB Connection: postgres://$DB_USER:$DB_PASSWORD@$DB_CONTAINER:$DB_PORT/$DB_NAME"
echo ""
echo "🎉 All set! To verify services:"
echo "    docker stack services $STACK_NAME"
echo "    docker ps --filter network=$NETWORK_NAME"

echo "=== Deployment complete ==="
