import logging
import logging_loki
import os

LOKI_URL = f"http://loki:{os.environ['LOKI_PORT']}"


def setup_logging(app):
    handler = logging_loki.LokiHandler(
        url=f"{LOKI_URL}/loki/api/v1/push",
        tags={"app": app},
        version="1",
    )
    logger = logging.getLogger()
    logger.setLevel(logging.INFO)
    logger.addHandler(handler)
