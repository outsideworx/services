#!/bin/bash

set -euo pipefail

VOLUMES=(
    "services_grafana:/home/outsideworx/grafana"
    "services_letsencrypt:/home/outsideworx/letsencrypt"
    "services_loki:/home/outsideworx/loki"
    "services_ntfy:/home/outsideworx/ntfy"
    "services_postgres:/home/outsideworx/data"
    "services_prometheus:/home/outsideworx/prometheus"
)

for entry in "${VOLUMES[@]}"; do
    volume="${entry%%:*}"
    host_path="${entry##*:}"

    if [ ! -d "$host_path" ]; then
        echo "SKIP: $host_path does not exist"
        continue
    fi

    echo "Migrating $host_path -> $volume"
    docker volume create "$volume"
    docker run --rm \
        -v "$host_path:/source:ro" \
        -v "$volume:/target" \
        alpine sh -c 'cp -a /source/. /target/'
    echo "  OK"
done

echo ""
echo "Migration complete. You can now run: bash deploy.sh"
