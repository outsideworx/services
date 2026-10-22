#!/bin/bash

set -e

if [ "$1" == "--install" ]; then
    apt update
    apt install -y docker-compose-v2 git maven npm openjdk-25-jdk
    exit 0
fi

if [ "$1" == "--network" ]; then
    if [ -n "$2" ]; then
        # Required ports:
        # 2377/tcp      - communication between nodes
        # 7946          - overlay network node discovery
        # 4789/udp      - overlay network traffic (configurable)
        # 51820/udp     - WireGuard gateway
        # 51821/tcp     - operator SSH
        docker swarm init --advertise-addr "$2"
    else
        echo "Warning: no IP address provided, skipping swarm init"
    fi
    docker network create -d overlay --attachable edge
    docker network create -d overlay --attachable outsideworx
    cat <<-EOF > /etc/modules-load.d/wg-easy.conf
			ip_tables
			iptable_nat
			ip6_tables
			ip6table_nat
		EOF
    systemctl restart systemd-modules-load.service
    exit 0
fi

if [ "$1" == "--secrets" ]; then
    if ! docker secret inspect rsa_private_key > /dev/null 2>&1; then
        openssl genrsa 4096 | docker secret create rsa_private_key -
    fi
    for secret in \
        grafana_oauth_client_secret \
        mailersend_sdk_token \
        services_oauth_client_secret; do
        if ! docker secret inspect "$secret" > /dev/null 2>&1; then
            read -rsp "$secret: " value
            echo
            printf '%s' "$value" | docker secret create "$secret" -
        fi
    done
    for secret in \
        app_clients_ciafo_token \
        app_clients_peeps_token \
        app_clients_soup_token \
        app_clients_worx_token \
        authelia_jwt_secret \
        authelia_oidc_hmac_secret \
        authelia_session_secret \
        authelia_storage_encryption_key \
        postgres_password \
        wg_metrics_token; do
        if ! docker secret inspect "$secret" > /dev/null 2>&1; then
            value=$(openssl rand -base64 32)
            echo "$secret: $value"
            printf '%s' "$value" | docker secret create "$secret" -
        fi
    done
    exit 0
fi

if [ -n "$1" ]; then
    echo "Error: Unknown parameter: '$1'"
    exit 1
fi

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
docker stack services services --format '{{.Name}}' | xargs -rn1 docker service update --force
