#!/bin/bash

SCRIPT_DIR=$(dirname "$(realpath "$0")")
DEST="/home/outsideworx/services"

set -e

if [ "$1" == "--authelia" ]; then
    cp "$SCRIPT_DIR/authelia-users.yaml" /home/outsideworx
    exit 0
fi

if [ "$1" == "--install" ]; then
    apt update
    apt install -y docker-compose-v2
    exit 0
fi

if [ "$1" == "--network" ]; then
    if [ -z "$2" ]; then
        echo "Error: an IP address as 2nd parameter is required"
        exit 1
    fi
    # Required open ports:
    # 2377/tcp      - communication with and between manager nodes
    # 7946/tcp+udp  - overlay network node discovery
    # 4789/udp      - overlay network traffic (configurable)
    docker swarm init --advertise-addr "$2"
    docker network create -d overlay --attachable outsideworx
    exit 0
fi

if [ "$1" == "--secrets" ]; then
    openssl genrsa 4096 | docker secret create rsa_private_key -
    exit 0
fi

if [ -n "$1" ]; then
    echo "Error: Unknown parameter: '$1'"
    exit 1
fi

rm -rf "$DEST"
mkdir -p "$DEST"
cp -r "$SCRIPT_DIR/utils" "$DEST"
cp "$SCRIPT_DIR/.env" \
   "$SCRIPT_DIR/authelia.yaml" \
   "$SCRIPT_DIR/compose.yaml" \
   "$SCRIPT_DIR/docker-stats.sh" \
   "$SCRIPT_DIR/docker-wipe.sh" \
   "$SCRIPT_DIR/grafana.ini" \
   "$SCRIPT_DIR/loki.yaml" \
   "$SCRIPT_DIR/logo.png" \
   "$SCRIPT_DIR/ntfy.yaml" \
   "$SCRIPT_DIR/prometheus.yaml" \
   "$DEST"

cd "$DEST"
set -a; source .env; set +a
docker stack deploy -c compose.yaml services --detach=false --resolve-image=always
