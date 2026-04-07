import base64
import logging
import os
import psycopg2
import time
from datetime import datetime
from common.logging_config import setup_logging

setup_logging("cache")

OUTPUT_DIR = "/utils/cache"
DB_HOST = "postgres"
DB_PORT = "5432"
DB_USER = os.environ["DB_USERNAME"]
DB_PASS = os.environ["DB_PASSWORD"]
DB_NAME = DB_USER
INTERVAL = 60

CIAFO_LABELS = ["image1", "image2", "image3", "image4", "thumbnail1", "thumbnail2", "thumbnail3", "thumbnail4"]
SOUP_LABELS = ["image", "thumbnail"]
HASH_FILE = f"{OUTPUT_DIR}/hashes.txt"
SCAN_TIME_FILE = f"{OUTPUT_DIR}/last_scan.txt"

os.makedirs(f"{OUTPUT_DIR}/ciafo", exist_ok=True)
os.makedirs(f"{OUTPUT_DIR}/soup", exist_ok=True)


def load_hashes():
    if not os.path.exists(HASH_FILE):
        return {}
    with open(HASH_FILE, "r") as f:
        hashes = {}
        for line in f:
            key, _, value = line.strip().partition("=")
            hashes[key] = value
        return hashes


def save_hashes(hashes):
    with open(HASH_FILE, "w") as f:
        for key, value in sorted(hashes.items()):
            f.write(f"{key}={value}\n")


def sync_table(cur, table, labels, subdir, hashes):
    cur.execute(f"SELECT id, hash FROM {table}")
    hash_rows = cur.fetchall()

    changed_ids = {id_ for id_, hash_ in hash_rows if hash_ and hashes.get(f"{subdir}:{id_}") != hash_}
    current_ids = {id_ for id_, _ in hash_rows}

    if changed_ids:
        cols = ", ".join(labels)
        cur.execute(f"SELECT id, {cols} FROM {table} WHERE id = ANY(%s)", (list(changed_ids),))
        active_files = set()
        for row in cur.fetchall():
            id_, *images = row
            for label, data in zip(labels, images):
                if not data:
                    continue
                path = f"{OUTPUT_DIR}/{subdir}/{id_}_{label}.jpg"
                active_files.add(os.path.basename(path))
                raw = data.split(",", 1)[1] if "," in data else data
                with open(path, "wb") as f:
                    f.write(base64.b64decode(raw))
                logging.info(f"Written {path}")

    for id_, hash_ in hash_rows:
        hashes[f"{subdir}:{id_}"] = hash_

    deleted_ids = {key.split(":")[1] for key in list(hashes) if key.startswith(f"{subdir}:") and int(key.split(":")[1]) not in current_ids}
    for id_ in deleted_ids:
        del hashes[f"{subdir}:{id_}"]
        for fname in os.listdir(f"{OUTPUT_DIR}/{subdir}"):
            if fname.startswith(f"{id_}_"):
                os.remove(f"{OUTPUT_DIR}/{subdir}/{fname}")
                logging.info(f"Removed {fname}")


def sync():
    hashes = load_hashes()
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    try:
        cur = conn.cursor()
        try:
            sync_table(cur, "ciafo", CIAFO_LABELS, "ciafo", hashes)
        except Exception as e:
            logging.error(f"CIAFO sync failed: {e}")
        try:
            sync_table(cur, "soup", SOUP_LABELS, "soup", hashes)
        except Exception as e:
            logging.error(f"SOUP sync failed: {e}")
        cur.close()
    finally:
        conn.close()
    save_hashes(hashes)
    with open(SCAN_TIME_FILE, "w") as f:
        f.write(datetime.now().isoformat())


while True:
    try:
        sync()
    except Exception as e:
        logging.error(f"Sync failed: {e}")
    time.sleep(INTERVAL)
