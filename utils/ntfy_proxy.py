import asyncio
import logging
import os
import sqlite3
from aiohttp import web, ClientSession, WSMsgType

from common.logging_config import setup_logging

setup_logging("ntfy-proxy")
logging.getLogger("aiohttp.access").setLevel(logging.WARNING)

NTFY_URL = os.environ["NTFY_URL"]
DB_PATH = os.environ["NTFY_DB_PATH"]
SKIP_REQUEST_HEADERS = {"host", "accept-encoding"}
SKIP_RESPONSE_HEADERS = {"content-length", "transfer-encoding", "content-encoding"}
SW_RESPONSE = web.Response(text="self.addEventListener('push', () => {});", content_type="application/javascript")


def load_tokens():
    try:
        conn = sqlite3.connect(f"file:{DB_PATH}?mode=ro", uri=True)
        cur = conn.cursor()
        cur.execute("""
            SELECT u.user, tk.token FROM user u
            JOIN user_token tk ON u.id = tk.user_id
            WHERE (tk.expires = 0 OR tk.expires >= strftime('%s', 'now'))
            ORDER BY tk.last_access DESC
        """)
        tokens = {}
        for username, token in cur.fetchall():
            tokens.setdefault(username, token)
        conn.close()
        for username in tokens:
            logging.info(f"Loaded token for user {username}")
        return tokens
    except Exception as e:
        logging.error(f"Failed to load tokens: {e}")
        return {}


def inject_autologin(body, username, token):
    script = f"""<script>
if (!localStorage.getItem('token')) {{
    localStorage.setItem('user', '{username}');
    localStorage.setItem('token', '{token}');
}}
</script>""".encode()
    return body.replace(b"<head>", b"<head>" + script)


TOKENS = load_tokens()


async def handle_ws(request, session, token):
    ws_server = web.WebSocketResponse()
    await ws_server.prepare(request)

    headers = {k: v for k, v in request.headers.items() if k.lower() not in SKIP_REQUEST_HEADERS | {"authorization"}}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    async with session.ws_connect(f"ws://ntfy{request.path_qs}", headers=headers) as ws_client:
        async def forward_to_client():
            async for msg in ws_client:
                if msg.type == WSMsgType.TEXT:
                    try:
                        await ws_server.send_str(msg.data)
                    except Exception:
                        break
                elif msg.type == WSMsgType.BINARY:
                    try:
                        await ws_server.send_bytes(msg.data)
                    except Exception:
                        break
                elif msg.type in (WSMsgType.CLOSE, WSMsgType.ERROR):
                    break

        async def forward_to_ntfy():
            async for msg in ws_server:
                if msg.type == WSMsgType.TEXT:
                    await ws_client.send_str(msg.data)
                elif msg.type == WSMsgType.BINARY:
                    await ws_client.send_bytes(msg.data)
                elif msg.type in (WSMsgType.CLOSE, WSMsgType.ERROR):
                    break

        tasks = [asyncio.ensure_future(forward_to_client()), asyncio.ensure_future(forward_to_ntfy())]
        done, pending = await asyncio.wait(tasks, return_when=asyncio.FIRST_COMPLETED)
        for task in pending:
            task.cancel()
            try:
                await task
            except asyncio.CancelledError:
                pass

    return ws_server


async def handle_http(request):
    user = request.headers.get("Remote-User")
    token = TOKENS.get(user) if user else None
    if not user:
        logging.warning("Request received with no Remote-User header")
    elif not token:
        logging.warning(f"No token found for user {user}")

    if request.path == "/sw.js":
        return SW_RESPONSE

    session = request.app["session"]

    if request.headers.get("Upgrade", "").lower() == "websocket":
        return await handle_ws(request, session, token)

    headers = {k: v for k, v in request.headers.items() if k.lower() not in SKIP_REQUEST_HEADERS}
    if token:
        headers["Authorization"] = f"Bearer {token}"

    async with session.request(
            request.method,
            f"{NTFY_URL}{request.path_qs}",
            headers=headers,
            data=await request.read() or None,
            allow_redirects=False,
    ) as resp:
        body = await resp.read()
        content_type = resp.headers.get("Content-Type", "")
        if token and "text/html" in content_type and b"<head>" in body:
            body = inject_autologin(body, user, token)
        response_headers = {k: v for k, v in resp.headers.items() if k.lower() not in SKIP_RESPONSE_HEADERS}
        return web.Response(status=resp.status, headers=response_headers, body=body)


async def on_startup(app):
    app["session"] = ClientSession()


async def on_cleanup(app):
    await app["session"].close()


app = web.Application()
app.on_startup.append(on_startup)
app.on_cleanup.append(on_cleanup)
app.router.add_route("*", "/{path_info:.*}", handle_http)

web.run_app(app, host="0.0.0.0", port=80)
