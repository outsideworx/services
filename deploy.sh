#!/bin/bash

SCRIPT_DIR=$(dirname "$(realpath "$0")")
DEST="/home/outsideworx/services"

if [ "$1" == "--letsencrypt" ]; then
    # WARNING: For this section to work, port 80 has to be open and accessible via the below mentioned address.
    certbot certonly --standalone --noninteractive --agree-tos --email info@outsideworx.net -d services.outsideworx.net
fi

echo "Packaging project"
mvn clean package -f "$SCRIPT_DIR/pom.xml"

echo "Copying project files to: $DEST"
rm -rf "$DEST"
mkdir -p "$DEST"
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
cp -r "$SCRIPT_DIR/target" "$DEST"

echo "Deployment starts"
cd "$DEST"
docker compose build --no-cache --pull
docker compose up --force-recreate --no-deps -d
echo "Sleep, to make sure everything is running"
sleep 10
docker system prune -af
docker logs services -f
