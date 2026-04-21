import logging
import logging_loki
import os

LOKI_URL = f"http://loki:{os.environ['LOKI_PORT']}"


def setup_logging(app):
    loki_handler = logging_loki.LokiHandler(
        url=f"{LOKI_URL}/loki/api/v1/push",
        tags={"app": app, "job": "utils"},
        version="1",
    )
    loki_handler.handleError = lambda record: None
    logger = logging.getLogger()
    logger.handlers.clear()
    logger.setLevel(logging.INFO)
    logger.addHandler(loki_handler)
    stream_handler = logging.StreamHandler()
    stream_handler.setFormatter(logging.Formatter(f"%(asctime)s %(levelname)s --- app={app}: %(message)s", datefmt="%Y-%m-%d %H:%M:%S"))
    logger.addHandler(stream_handler)
