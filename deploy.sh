#!/bin/bash

SCRIPT_DIR=$(dirname "$(realpath "$0")")
DEST="/home/outsideworx/services"

set -e

if [ "$1" == "--install" ]; then
    apt update
    apt install -y docker-compose-v2 openjdk-25-jdk maven
    exit 0
fi

if [ -n "$1" ]; then
    echo "Error: Unknown parameter '$1'."
    exit 1
fi

echo "Packaging and installing project to: $DEST."
rm -rf "$DEST"
mkdir -p "$DEST"
mvn clean package -f "$SCRIPT_DIR/pom.xml"
cp -r "$SCRIPT_DIR/target" "$DEST"

echo "Copying standalone project files to: $DEST"
cp "$SCRIPT_DIR/.env" \
   "$SCRIPT_DIR/compose.yaml" \
   "$SCRIPT_DIR/docker-stats.sh" \
   "$SCRIPT_DIR/docker-wipe.sh" \
   "$SCRIPT_DIR/Dockerfile" \
   "$SCRIPT_DIR/grafana.ini" \
   "$SCRIPT_DIR/loki.yaml" \
   "$SCRIPT_DIR/ntfy.yaml" \
   "$SCRIPT_DIR/prometheus.yaml" \
   "$DEST"

echo "Container deployment starts."
cd "$DEST"
docker compose build --no-cache --pull
docker compose up --force-recreate --no-deps -d
echo "Sleep, to make sure everything is running."
sleep 10
docker system prune -af
docker logs services -f
