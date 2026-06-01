#!/bin/bash

SCRIPT_DIR=$(dirname "$(realpath "$0")")
DEST="/home/outsideworx/services"

set -e

if [ "$1" == "--install" ]; then
    apt update
    apt install -y docker-compose-v2
    exit 0
fi

if [ "$1" == "--network" ]; then
    if [ -n "$2" ]; then
        # Required open ports:
        # 2377/tcp      - communication with and between manager nodes
        # 7946/tcp+udp  - overlay network node discovery
        # 4789/udp      - overlay network traffic (configurable)
        docker swarm init --advertise-addr "$2"
    else
        echo "Warning: no IP address provided, skipping swarm init"
    fi
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

mkdir -p "$DEST"
mkdir -p /home/outsideworx/utils
cp -r "$SCRIPT_DIR/utils" "$DEST"
cp "$SCRIPT_DIR/.env" \
   "$SCRIPT_DIR/authelia.yaml" \
   "$SCRIPT_DIR/authelia-users.yaml" \
   "$SCRIPT_DIR/compose.yaml" \
   "$SCRIPT_DIR/grafana.ini" \
   "$SCRIPT_DIR/loki.yaml" \
   "$SCRIPT_DIR/logo.png" \
   "$SCRIPT_DIR/ntfy.yaml" \
   "$SCRIPT_DIR/prometheus.yaml" \
   "$SCRIPT_DIR/promtail.yaml" \
   "$DEST"

cd "$DEST"
set -a; source .env; set +a
export HASH_AUTHELIA=$(sha256sum authelia.yaml | cut -c1-8)
export HASH_AUTHELIA_USERS=$(sha256sum authelia-users.yaml | cut -c1-8)
export HASH_GRAFANA=$(sha256sum grafana.ini | cut -c1-8)
export HASH_LOGO=$(sha256sum logo.png | cut -c1-8)
export HASH_LOKI=$(sha256sum loki.yaml | cut -c1-8)
export HASH_NTFY=$(sha256sum ntfy.yaml | cut -c1-8)
export HASH_PROMETHEUS=$(sha256sum prometheus.yaml | cut -c1-8)
export HASH_PROMTAIL=$(sha256sum promtail.yaml | cut -c1-8)

docker compose pull
docker stack deploy -c compose.yaml services --detach=false --resolve-image=always
docker stack services services --format '{{.Name}}' | xargs -I{} docker service update --force {}
