#!/bin/bash

SCRIPT_DIR=$(dirname "$(realpath "$0")")
DEST="/home/outsideworx/services"

set -e

if [ "$1" == "--install" ]; then
    apt update
    apt install -y docker-compose-v2 openjdk-25-jdk maven
    exit 0
fi

if [ "$1" == "--network" ]; then
    if [ -z "$2" ]; then
        echo "Error: an IP address as 2nd parameter is required."
        exit 1
    fi
    # Required open ports:
    # 2377/tcp      - communication with and between manager nodes
    # 7946/tcp+udp  - overlay network node discovery
    # 4789/udp      - overlay network traffic (configurable)
    docker swarm init --advertise-addr "$2"
    docker network create -d overlay --attachable outsideworx
    docker service create \
        --name keepalive \
        --network outsideworx \
        --mode global \
        --restart-condition any \
        --update-order start-first \
        alpine:3.21 sleep infinity
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
cp -r "$SCRIPT_DIR/utils" "$DEST"

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
docker login
docker compose build --no-cache --pull
docker compose push
docker compose up --force-recreate --no-deps -d
echo "Sleep, to make sure everything is running."
sleep 10
docker system prune -af
docker logs services -f
