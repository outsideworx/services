#!/bin/bash

RESET='\033[0m'
BOLD='\033[1m'
CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
DIRTY_WHITE='\033[0;37m'
RED='\033[0;31m'

to_mb() {
    local val="$1"
    local num unit
    num=$(echo "$val" | sed 's/[^0-9.]//g')
    unit=$(echo "$val" | sed 's/[0-9.]//g' | tr '[:lower:]' '[:upper:]')
    case "$unit" in
        B)       awk "BEGIN {printf \"%.2f\", $num / 1024^2}" ;;
        KB|KIB)  awk "BEGIN {printf \"%.2f\", $num / 1024}" ;;
        MB|MIB)  awk "BEGIN {printf \"%.2f\", $num}" ;;
        GB|GIB)  awk "BEGIN {printf \"%.2f\", $num * 1024}" ;;
        TB|TIB)  awk "BEGIN {printf \"%.2f\", $num * 1024^2}" ;;
        *)       echo "0.00" ;;
    esac
}

to_gb() {
    local val="$1"
    local num unit
    num=$(echo "$val" | sed 's/[^0-9.]//g')
    unit=$(echo "$val" | sed 's/[0-9.]//g' | tr '[:lower:]' '[:upper:]')
    case "$unit" in
        B)       awk "BEGIN {printf \"%.2f\", $num / 1024^3}" ;;
        KB|KIB)  awk "BEGIN {printf \"%.2f\", $num / 1024^2}" ;;
        MB|MIB)  awk "BEGIN {printf \"%.2f\", $num / 1024}" ;;
        GB|GIB)  awk "BEGIN {printf \"%.2f\", $num}" ;;
        TB|TIB)  awk "BEGIN {printf \"%.2f\", $num * 1024}" ;;
        *)       echo "0.00" ;;
    esac
}

color_pct() {
    local val="$1"
    if awk "BEGIN {exit !($val >= 80)}"; then echo "$RED"
    elif awk "BEGIN {exit !($val >= 20)}"; then echo "$YELLOW"
    else echo "$DIRTY_WHITE"
    fi
}

color_gb() {
    local val="$1"
    if awk "BEGIN {exit !($val >= 10)}"; then echo "$RED"
    elif awk "BEGIN {exit !($val >= 2)}"; then echo "$YELLOW"
    else echo "$DIRTY_WHITE"
    fi
}

C1=30; C2=7; C3=11; C4=13; C5=13
SEP=2

total=$(( C1 + SEP + C2 + SEP + C3 + SEP + C4 + SEP + C5 + 2 ))

stats_data=$(docker stats --no-stream --format \
    "{{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}\t{{.NetIO}}")

printf "\n"
printf "${BOLD}  %-${C1}s  %${C2}s  %${C3}s  %${C4}s  %${C5}s${RESET}\n" \
    "CONTAINER" "CPU %" "RAM (MB)" "NET IN (GB)" "NET OUT (GB)"
printf "  %s\n" "$(printf '%.0s-' $(seq 1 $total))"

total_cpu=0
total_ram_mb=0

while IFS=$'\t' read -r name cpu mem mem_pct netio; do
    net_in=$(echo  "$netio" | awk '{print $1}')
    net_out=$(echo "$netio" | awk '{print $3}')
    ram=$(echo     "$mem"   | awk '{print $1}')

    net_in_gb=$(to_gb  "$net_in")
    net_out_gb=$(to_gb "$net_out")
    ram_mb=$(to_mb     "$ram")

    cpu_num=$(echo "$cpu" | sed 's/%//')
    mem_num=$(echo "$mem_pct" | sed 's/%//')

    cpu_color=$(color_pct "$cpu_num")
    ram_color=$(color_pct "$mem_num")
    net_in_color=$(color_gb  "$net_in_gb")
    net_out_color=$(color_gb "$net_out_gb")

    if [ ${#name} -gt 26 ]; then
        name="${name:0:23}..."
    fi

    printf "  ${CYAN}%-${C1}s${RESET}  ${cpu_color}%${C2}s${RESET}  ${ram_color}%${C3}s${RESET}  ${net_in_color}%${C4}s${RESET}  ${net_out_color}%${C5}s${RESET}\n" \
        "$name" "$cpu" "${ram_mb} MB" "${net_in_gb} GB" "${net_out_gb} GB"

    total_cpu=$(awk "BEGIN {printf \"%.2f\", $total_cpu + $cpu_num}")
    total_ram_mb=$(awk "BEGIN {printf \"%.2f\", $total_ram_mb + $ram_mb}")
done <<< "$stats_data"

printf "\n"

host_mem_total_kb=$(awk '/MemTotal/ {print $2}' /proc/meminfo)
host_mem_total_mb=$(awk "BEGIN {printf \"%.2f\", $host_mem_total_kb / 1024}")
total_ram_pct=$(awk "BEGIN {printf \"%.2f\", ($total_ram_mb / $host_mem_total_mb) * 100}")

cpu_color=$(color_pct "$total_cpu")
ram_color=$(color_pct "$total_ram_pct")

printf "${BOLD}  Summary${RESET}\n"
printf "  %s\n" "$(printf '%.0s-' $(seq 1 $total))"
printf "  %-${C1}s  ${cpu_color}%${C2}s${RESET}  ${ram_color}%${C3}s${RESET}\n" \
    "" "${total_cpu}%" "${total_ram_mb} MB"


stopped=$(docker ps --filter "status=exited" --filter "status=dead" --format "{{.Names}}\t{{.Status}}")

printf "${BOLD}  Checks${RESET}\n"
printf "  %s\n" "$(printf '%.0s-' $(seq 1 $total))"


if [ -n "$stopped" ]; then
    printf "${BOLD}${RED}  ⚠  Stopped containers:${RESET}\n"
    while IFS=$'\t' read -r name status; do
        printf "  ${RED}%-30s${RESET}  %s\n" "$name" "$status"
    done <<< "$stopped"
else
    printf "${GREEN}  ✔  All containers are running.${RESET}\n"
fi

printf "\n"
