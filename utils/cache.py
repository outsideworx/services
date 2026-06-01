import base64
import logging
import os
import psycopg2
import psycopg2.errors
import time
from datetime import datetime

from commons.logging_config import setup_logging

setup_logging("cache")

OUTPUT_DIR = "/utils/cache"

CIAFO_LABELS = ["image1", "image2", "image3", "image4", "thumbnail1", "thumbnail2", "thumbnail3", "thumbnail4"]
DB_HOST = "services_postgres"
DB_NAME = os.environ["DB_USERNAME"]
DB_PASS = os.environ["DB_PASSWORD"]
DB_PORT = "5432"
DB_USER = os.environ["DB_USERNAME"]
HASH_FILE = f"{OUTPUT_DIR}/hashes.properties"
INTERVAL = 60
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


def sync_categories(cur, table, subdir):
    cur.execute(f"SELECT id, category FROM {table} ORDER BY id")
    categories = {}
    for id_, category in cur.fetchall():
        categories.setdefault(category, []).append(id_)

    path = f"{OUTPUT_DIR}/{subdir}/categories.properties"
    with open(path, "w") as f:
        for index, category in enumerate(sorted(categories.keys()), 1):
            ids = ",".join(str(id_) for id_ in categories[category])
            f.write(f"{index}={category}\n")
            f.write(f"{index}.ids={ids}\n")

    category_index = {category: index for index, category in enumerate(sorted(categories.keys()), 1)}
    logging.info(f"Written category index for {subdir}: {list(categories.keys())}")
    return category_index


def remove_files_for_item(subdir, id_):
    for filename in os.listdir(f"{OUTPUT_DIR}/{subdir}"):
        parts = filename.split("_")
        if len(parts) >= 3 and parts[1] == str(id_):
            os.remove(f"{OUTPUT_DIR}/{subdir}/{filename}")
            logging.info(f"Removed {filename}")


def sync_images_to_disk(cur, table, labels, subdir, hashes, category_index):
    cur.execute(f"SELECT id, category, hash FROM {table}")
    rows = cur.fetchall()

    changed_ids = {id_ for id_, _, hash_ in rows if hash_ and hashes.get(f"{subdir}.{id_}") != hash_}
    current_ids = {id_ for id_, _, _ in rows}
    category_by_id = {id_: category_index.get(category, 0) for id_, category, _ in rows}

    if changed_ids:
        columns = ", ".join(labels)
        cur.execute(f"SELECT id, {columns} FROM {table} WHERE id = ANY(%s)", (list(changed_ids),))
        for row in cur.fetchall():
            id_, *images = row
            remove_files_for_item(subdir, id_)
            category_id = category_by_id.get(id_, 0)
            for label, encoded_image in zip(labels, images):
                if not encoded_image:
                    continue
                path = f"{OUTPUT_DIR}/{subdir}/{category_id}_{id_}_{label}.jpg"
                base64_data = encoded_image.split(",", 1)[1] if "," in encoded_image else encoded_image
                with open(path, "wb") as f:
                    f.write(base64.b64decode(base64_data))
                logging.info(f"Written {path}")

    for id_, _, hash_ in rows:
        hashes[f"{subdir}.{id_}"] = hash_

    for hash_key in list(hashes):
        if not hash_key.startswith(f"{subdir}."):
            continue
        id_ = int(hash_key.split(".")[1])
        if id_ not in current_ids:
            del hashes[hash_key]
            remove_files_for_item(subdir, id_)


def sync():
    hashes = load_hashes()
    conn = psycopg2.connect(host=DB_HOST, port=DB_PORT, dbname=DB_NAME, user=DB_USER, password=DB_PASS)
    conn.autocommit = True
    try:
        cur = conn.cursor()
        try:
            category_index = sync_categories(cur, "ciafo", "ciafo")
            sync_images_to_disk(cur, "ciafo", CIAFO_LABELS, "ciafo", hashes, category_index)
            with open(f"{OUTPUT_DIR}/ciafo/last_scan.txt", "w") as f:
                f.write(datetime.now().isoformat())
        except psycopg2.errors.UndefinedTable:
            logging.error("CIAFO sync failed: table not found")
        except Exception as e:
            logging.error(f"CIAFO sync failed: {str(e).strip()}")
        try:
            category_index = sync_categories(cur, "soup", "soup")
            sync_images_to_disk(cur, "soup", SOUP_LABELS, "soup", hashes, category_index)
            with open(f"{OUTPUT_DIR}/soup/last_scan.txt", "w") as f:
                f.write(datetime.now().isoformat())
        except psycopg2.errors.UndefinedTable:
            logging.error("SOUP sync failed: table not found")
        except Exception as e:
            logging.error(f"SOUP sync failed: {str(e).strip()}")
        cur.close()
    finally:
        conn.close()
    save_hashes(hashes)


while True:
    try:
        sync()
    except Exception as e:
        logging.error(f"Sync failed: {str(e).strip()}")
    time.sleep(INTERVAL)
