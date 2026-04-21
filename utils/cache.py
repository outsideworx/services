import base64
import logging
import os
import psycopg2
import psycopg2.errors
import time
from datetime import datetime

from common.logging_config import setup_logging

setup_logging("cache")

DB_NAME = os.environ["DB_USERNAME"]
DB_PASS = os.environ["DB_PASSWORD"]
DB_USER = os.environ["DB_USERNAME"]
DB_HOST = "postgres"
DB_PORT = "5432"
INTERVAL = 60
OUTPUT_DIR = "/utils/cache"
CIAFO_LABELS = ["image1", "image2", "image3", "image4", "thumbnail1", "thumbnail2", "thumbnail3", "thumbnail4"]
HASH_FILE = f"{OUTPUT_DIR}/hashes.txt"
SCAN_TIME_FILE = f"{OUTPUT_DIR}/last_scan.txt"
SOUP_LABELS = ["image", "thumbnail"]

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


def sync_images_to_disk(cur, table, labels, subdir, hashes):
    cur.execute(f"SELECT id, hash FROM {table}")
    db_rows = cur.fetchall()

    changed_ids = {id_ for id_, hash_ in db_rows if hash_ and hashes.get(f"{subdir}:{id_}") != hash_}
    current_ids = {id_ for id_, _ in db_rows}

    if changed_ids:
        cols = ", ".join(labels)
        cur.execute(f"SELECT id, {cols} FROM {table} WHERE id = ANY(%s)", (list(changed_ids),))
        for row in cur.fetchall():
            id_, *images = row
            for label, encoded_image in zip(labels, images):
                if not encoded_image:
                    continue
                path = f"{OUTPUT_DIR}/{subdir}/{id_}_{label}.jpg"
                base64_data = encoded_image.split(",", 1)[1] if "," in encoded_image else encoded_image
                with open(path, "wb") as f:
                    f.write(base64.b64decode(base64_data))
                logging.info(f"Written {path}")

    for id_, hash_ in db_rows:
        hashes[f"{subdir}:{id_}"] = hash_

    for hash_key in list(hashes):
        if not hash_key.startswith(f"{subdir}:"):
            continue
        id_ = hash_key.split(":")[1]
        if int(id_) not in current_ids:
            del hashes[hash_key]
            for fname in os.listdir(f"{OUTPUT_DIR}/{subdir}"):
                if fname.startswith(f"{id_}_"):
                    os.remove(f"{OUTPUT_DIR}/{subdir}/{fname}")
                    logging.info(f"Removed {fname}")


def sync():
    hashes = load_hashes()
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    success = True
    try:
        cur = conn.cursor()
        try:
            sync_images_to_disk(cur, "ciafo", CIAFO_LABELS, "ciafo", hashes)
        except psycopg2.errors.UndefinedTable:
            logging.error("CIAFO sync failed: table not found")
            success = False
        except Exception as e:
            logging.error(f"CIAFO sync failed: {str(e).strip()}")
            success = False
        try:
            sync_images_to_disk(cur, "soup", SOUP_LABELS, "soup", hashes)
        except psycopg2.errors.UndefinedTable:
            logging.error("SOUP sync failed: table not found")
            success = False
        except Exception as e:
            logging.error(f"SOUP sync failed: {str(e).strip()}")
            success = False
        cur.close()
    finally:
        conn.close()
    save_hashes(hashes)
    if success:
        with open(SCAN_TIME_FILE, "w") as f:
            f.write(datetime.now().isoformat())


while True:
    try:
        sync()
    except Exception as e:
        logging.error(f"Sync failed: {str(e).strip()}")
    time.sleep(INTERVAL)
