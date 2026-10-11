import http.server
import socketserver
import os
import re
import socket
import subprocess
import threading
import sys
import time

PORT = 8085
PROJECT_ROOT = os.path.dirname(os.path.abspath(__file__))
APK_PATH = os.path.join(PROJECT_ROOT, "app", "build", "outputs", "apk", "debug", "app-debug.apk")

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

LOCAL_IP = get_local_ip()

HTML_PAGE = """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NothingTune — Music Player APK</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
        }
        body {
            background: #000000;
            color: #ffffff;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .card {
            background: #111114;
            border: 1px solid #262626;
            border-radius: 24px;
            max-width: 440px;
            width: 100%;
            padding: 32px 24px;
            text-align: center;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.8);
        }
        .icon-badge {
            width: 76px;
            height: 76px;
            background: #1e1e24;
            border: 1px solid #333338;
            border-radius: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 20px;
            font-size: 34px;
        }
        h1 {
            font-size: 24px;
            font-weight: 800;
            margin-bottom: 6px;
            letter-spacing: 1px;
        }
        .subtitle {
            font-size: 13px;
            color: #888888;
            margin-bottom: 22px;
        }
        .meta-badges {
            display: flex;
            gap: 8px;
            justify-content: center;
            margin-bottom: 24px;
            flex-wrap: wrap;
        }
        .badge {
            background: #1a1a1f;
            border: 1px solid #2a2a30;
            padding: 5px 12px;
            border-radius: 16px;
            font-size: 11px;
            color: #cccccc;
            font-weight: 600;
            letter-spacing: 0.5px;
        }
        .btn-download {
            display: block;
            width: 100%;
            padding: 16px;
            background: #ffffff;
            color: #000000;
            font-size: 17px;
            font-weight: 800;
            text-decoration: none;
            border-radius: 14px;
            transition: all 0.2s ease;
            margin-bottom: 24px;
            letter-spacing: 0.5px;
        }
        .btn-download:hover {
            background: #e0e0e0;
            transform: translateY(-2px);
        }
        .steps {
            background: #16161a;
            border: 1px solid #222226;
            border-radius: 16px;
            padding: 16px;
            text-align: left;
            margin-bottom: 20px;
        }
        .steps h3 {
            font-size: 12px;
            color: #ffffff;
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: 1px;
        }
        .step-item {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            font-size: 12px;
            color: #aaaaaa;
            margin-bottom: 8px;
            line-height: 1.4;
        }
        .step-item:last-child {
            margin-bottom: 0;
        }
        .step-num {
            background: #2a2a30;
            color: #ffffff;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 10px;
            font-weight: 700;
            flex-shrink: 0;
            margin-top: 1px;
        }
        .qr-section {
            margin-top: 20px;
            padding-top: 20px;
            border-top: 1px solid #222226;
        }
        .qr-section img {
            border-radius: 12px;
            background: white;
            padding: 8px;
            margin-bottom: 8px;
        }
        .qr-hint {
            font-size: 11px;
            color: #777777;
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="icon-badge">🎵</div>
        <h1>NOTHING TUNE</h1>
        <p class="subtitle">Nothing OS Themed Hi-Res Music Player</p>

        <div class="meta-badges">
            <span class="badge">Version 1.0.0</span>
            <span class="badge">APK ~21 MB</span>
            <span class="badge">Hi-Res 192kHz</span>
            <span class="badge">10-Band EQ</span>
        </div>

        <a href="/NothingTune.apk" class="btn-download" download="NothingTune.apk">
            ⬇️ Download NothingTune APK
        </a>

        <div class="steps">
            <h3>Installation Instructions</h3>
            <div class="step-item">
                <span class="step-num">1</span>
                <span>Tap <strong>Download NothingTune APK</strong> above.</span>
            </div>
            <div class="step-item">
                <span class="step-num">2</span>
                <span>If prompted with <em>"File might be harmful"</em>, tap <strong>Download anyway</strong>.</span>
            </div>
            <div class="step-item">
                <span class="step-num">3</span>
                <span>Tap the downloaded file to <strong>Open &amp; Install</strong> (allow <em>Install unknown apps</em> if requested).</span>
            </div>
        </div>

        <div class="qr-section">
            <img id="qr-img" width="150" height="150" alt="QR Code" />
            <p class="qr-hint">Scan with your phone camera to download</p>
        </div>
    </div>

    <script>
        const currentUrl = window.location.href;
        const qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=" + encodeURIComponent(currentUrl);
        document.getElementById("qr-img").src = qrUrl;
    </script>
</body>
</html>
"""

class ApkHandler(http.server.BaseHTTPRequestHandler):
    def do_HEAD(self):
        self.handle_response(head_only=True)

    def do_GET(self):
        self.handle_response(head_only=False)

    def handle_response(self, head_only=False):
        path = self.path.split("?")[0]

        if path in ("/NothingTune.apk", "/SmartTools.apk", "/app-debug.apk"):
            if not os.path.exists(APK_PATH):
                self.send_error(404, "APK not found. Please build the APK first.")
                return

            file_size = os.path.getsize(APK_PATH)
            self.send_response(200)
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Disposition", 'attachment; filename="NothingTune.apk"')
            self.send_header("Content-Length", str(file_size))
            self.send_header("Cache-Control", "no-cache")
            self.end_headers()

            if not head_only:
                with open(APK_PATH, "rb") as f:
                    while chunk := f.read(64 * 1024):
                        self.wfile.write(chunk)
            return

        # Default: Serve HTML landing page
        content = HTML_PAGE.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(content)))
        self.send_header("Cache-Control", "no-cache")
        self.end_headers()

        if not head_only:
            self.wfile.write(content)

    def log_message(self, format, *args):
        sys.stdout.write(f"[{time.strftime('%H:%M:%S')}] {args[0]} {args[1]} -> {args[2]}\\n")
        sys.stdout.flush()

class ThreadingHTTPServer(socketserver.ThreadingMixIn, socketserver.TCPServer):
    allow_reuse_address = True

def start_http_server():
    server = ThreadingHTTPServer(("0.0.0.0", PORT), ApkHandler)
    server.serve_forever()

def start_tunnel():
    print(f"\\n=======================================================")
    print(f">> NOTHING TUNE APK SERVER RUNNING (Port {PORT})")
    print(f"=======================================================")
    print(f"\\n[1] LOCAL WI-FI ACCESS (Phone on same Wi-Fi):")
    print(f"    http://{LOCAL_IP}:{PORT}")
    print(f"    Direct APK: http://{LOCAL_IP}:{PORT}/NothingTune.apk")
    print(f"\\n[2] INITIALIZING ONLINE PUBLIC ACCESS TUNNEL...")

    try:
        proc = subprocess.Popen(
            ["ssh", "-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10",
             "-R", f"80:127.0.0.1:{PORT}", "nokey@localhost.run"],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1
        )

        for line in proc.stdout:
            match = re.search(r'(https://[a-zA-Z0-9-]+\\.lhr\\.life)', line)
            if match:
                online_url = match.group(1)
                print(f"\\n[2] ONLINE GLOBAL ACCESS (Phone on Mobile Data or any network):")
                print(f"    {online_url}")
                print(f"    Direct APK: {online_url}/NothingTune.apk")
                print(f"\\n>> Open either link above in your phone's browser to download!")
                print(f"=======================================================\\n")
                break
    except Exception as e:
        print(f"Note: Public tunnel failed ({e}). Use Local Wi-Fi link above.")

if __name__ == "__main__":
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8")
    if hasattr(sys.stderr, "reconfigure"):
        sys.stderr.reconfigure(encoding="utf-8")
    t_server = threading.Thread(target=start_http_server, daemon=True)
    t_server.start()

    start_tunnel()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\\nServer stopped.")
