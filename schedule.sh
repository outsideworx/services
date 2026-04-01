#!/bin/bash

SCRIPT_PATH=$(realpath "$0")

if [ "$1" == "--apply" ]; then
    CRON_JOB="0 * * * * bash $SCRIPT_PATH"
    (crontab -l 2>/dev/null | grep -v "$SCRIPT_PATH"; echo "$CRON_JOB") | crontab -
    echo "Cronjob installed: runs every hour"
    exit 0
fi

if [ -n "$1" ]; then
    echo "Error: Unknown parameter '$1'"
    exit 1
fi

source <(grep -A999 'services-ntfy' "$HOME/.bashrc" | head -n $(grep -c '' <(sed -n '/services-ntfy/,/^}/p' "$HOME/.bashrc")))

PARENT_DIR=$(dirname "$(dirname "$SCRIPT_PATH")")
HASH_FILE="$PARENT_DIR/commits.txt"

touch "$HASH_FILE"

for dir in "$PARENT_DIR"/*/ "$PARENT_DIR"/.github/; do
    [ ! -d "$dir/.git" ] && continue

    repo=$(basename "$dir")
    old_hash=$(grep "^$repo " "$HASH_FILE" 2>/dev/null | awk '{print $2}')
    new_hash=$(git -C "$dir" rev-parse HEAD)

    if [ "$old_hash" != "$new_hash" ]; then
        git -C "$dir" pull
        services-ntfy "Update detected on $repo"

        if grep -q "^$repo " "$HASH_FILE"; then
            sed -i "s/^$repo .*/$repo $new_hash/" "$HASH_FILE"
        else
            echo "$repo $new_hash" >> "$HASH_FILE"
        fi
    fi
done
