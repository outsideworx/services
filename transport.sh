#!/bin/bash

SCRIPT_DIR=$(dirname "$(realpath "$0")")
if [ -z "$2" ]; then
    echo "Error: IP address is required as the second parameter!"
    exit 1
fi
SERVER_IP="$2"

if [ "$1" == "--download" ]; then
    echo "Downloading files: $SERVER_IP"
    rsync -rvh --delete root@"$SERVER_IP":/home/outsideworx /tmp;
elif [ "$1" == "--deploy" ]; then
    echo "Packaging project"
    mvn clean package -f "$SCRIPT_DIR/pom.xml"
    echo "Uploading project: $SERVER_IP"
    rsync -rvh --delete \
        "$SCRIPT_DIR/.env" \
        "$SCRIPT_DIR/compose.override.yaml" \
        "$SCRIPT_DIR/compose.yaml" \
        "$SCRIPT_DIR/Dockerfile" \
        "$SCRIPT_DIR/Dockerfile.site" \
        "$SCRIPT_DIR/grafana.ini" \
        "$SCRIPT_DIR/loki.yaml" \
        "$SCRIPT_DIR/ntfy.yaml" \
        "$SCRIPT_DIR/prometheus.yaml" \
        "$SCRIPT_DIR/target" \
        root@"$SERVER_IP":/home/outsideworx/services
    echo "Deployment starts: $SERVER_IP"
    ssh root@"$SERVER_IP" "
        cd /home/outsideworx/services;
        docker compose build --no-cache --pull
        docker compose up --force-recreate --no-deps -d;
        docker system prune -af;
        docker logs vault -f"
else
    echo "Error: Only download & deploy modes are supported!"
    exit 1
fi
