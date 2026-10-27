#!/bin/bash

set -e

for arg in "${@:2}"; do
    if [ "$arg" == "--host" ]; then
        echo "Error: --host must be the first parameter"
        exit 1
    fi
done

if [ "$1" == "--host" ]; then
    if [ -z "$2" ]; then
        echo "Error: --host requires a target (e.g. user@server)"
        exit 1
    fi
    # Only --network is self-contained: --secrets must run on the Swarm manager
    # it targets, and the deployment needs the repo's config files and .env.
    if [ "$3" != "--network" ]; then
        echo "Error: --host only supports --network (e.g. --host user@server --network <IP>)"
        exit 1
    fi
    HOST="$2"
    shift 2
    SCRIPT_NAME=$(basename "$0")
    scp "$0" "$HOST:~/$SCRIPT_NAME"
    ssh "$HOST" "cd ~ && ./$SCRIPT_NAME $(printf '%q ' "$@")"
    exit 0
fi

if [ "$1" == "--network" ]; then
    if [ -n "$3" ]; then
        echo "Error: Unknown parameter: '$3'"
        exit 1
    fi
    if [ -n "$2" ]; then
        # Required open ports:
        # 2377/tcp      - communication with and between manager nodes
        # 7946          - overlay network node discovery
        # 4789/udp      - overlay network traffic (configurable)
        # 51820/udp     - WireGuard tunnel (host mode, public)
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
    wg_metrics_token=$(openssl rand -base64 32)
    echo "wg_metrics_token: $wg_metrics_token"
    echo "$wg_metrics_token" | docker secret create wg_metrics_token -
    openssl genrsa 4096 | docker secret create rsa_private_key -
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
