"""
Test asap versi browser: muat game lewat CheerpJ di Chromium, pastikan menu tampil
dan klik START benar-benar berpindah ke layar pilih level.

  python tools/web_smoke.py dist/web dist/web/check

Halaman disajikan di sub-folder /pvz/ (seperti di portofolio) untuk menguji perhitungan path JAR.
Keluar dengan kode 1 bila gagal. Butuh: playwright, pillow, dan akses internet (CheerpJ dari CDN).
"""
import json
import shutil
import sys
import tempfile
import threading
import time
from pathlib import Path

from PIL import Image, ImageChops, ImageStat
from playwright.sync_api import sync_playwright

sys.path.insert(0, str(Path(__file__).parent))
from serve import make_server  # noqa: E402

SCALE = 1.5
START = (328 * SCALE, 322 * SCALE)    # tombol START di menu
LEVEL_1 = (123 * SCALE, 120 * SCALE)  # kartu Level 1 di layar pilih level


def brightness(path):
    return ImageStat.Stat(Image.open(path).convert("L")).mean[0]


def differ(a, b):
    diff = ImageChops.difference(Image.open(a).convert("L"), Image.open(b).convert("L"))
    return ImageStat.Stat(diff).mean[0]


def main(site, out):
    site, out = Path(site), Path(out)
    out.mkdir(parents=True, exist_ok=True)
    root = Path(tempfile.mkdtemp())
    shutil.copytree(site, root / "pvz", ignore=shutil.ignore_patterns("check"))
    srv = make_server(root)
    threading.Thread(target=srv.serve_forever, daemon=True).start()
    url = f"http://127.0.0.1:{srv.server_address[1]}/pvz/?autoplay=1"

    report = {"url_path": "/pvz/", "checks": {}, "page_errors": [], "console_errors": []}
    console_all = []
    ok = True
    with sync_playwright() as p:
        b = p.chromium.launch()
        page = b.new_page(viewport={"width": 1280, "height": 900})
        page.on("pageerror", lambda e: report["page_errors"].append(str(e)))
        page.on("console", lambda m: (console_all.append(f"[{m.type}] {m.text}"),
                                      m.type == "error" and report["console_errors"].append(m.text)))
        page.on("requestfailed", lambda r: console_all.append(f"[requestfailed] {r.url} {r.failure}"))
        t0 = time.time()
        page.goto(url)
        try:
            page.wait_for_function("window.__pvzReady === true", timeout=240_000)
        except Exception as e:
            report["checks"]["game_loaded"] = False
            page.screenshot(path=str(out / "gagal.png"))
            print("Game tidak termuat:", e)
            print("Overlay:", page.evaluate("document.getElementById('overlay').innerText"))
            print("Isi #display:", page.evaluate("document.getElementById('display').innerHTML.slice(0, 1500)"))
            print("Elemen di body:", page.evaluate("[...document.body.querySelectorAll('*')].map(e=>e.tagName).filter(t=>!['P','LI','A','B','KBD','SPAN','I','CODE','UL','H3','H1','H2','DIV'].includes(t)).join(',')"))
            print("Log konsol (60 terakhir):")
            for line in console_all[-60:]:
                print("  ", line[:300])
            ok = False
        if ok:
            report["seconds_to_ready"] = round(time.time() - t0, 1)
            report["checks"]["game_loaded"] = True
            page.wait_for_timeout(4000)
            stage = page.locator("#stage")
            stage.screenshot(path=str(out / "menu.png"))
            page.screenshot(path=str(out / "halaman.png"))
            report["checks"]["menu_not_blank"] = brightness(out / "menu.png") > 25

            stage.click(position={"x": START[0], "y": START[1]})
            page.wait_for_timeout(2500)
            stage.screenshot(path=str(out / "pilih-level.png"))
            report["checks"]["start_changes_screen"] = differ(out / "menu.png", out / "pilih-level.png") > 10

            stage.click(position={"x": LEVEL_1[0], "y": LEVEL_1[1]})
            page.wait_for_timeout(2500)
            stage.screenshot(path=str(out / "inventory.png"))
            report["checks"]["level_click_changes_screen"] = differ(out / "pilih-level.png", out / "inventory.png") > 10
        report["checks"]["no_page_errors"] = not report["page_errors"]
        b.close()
    srv.shutdown()

    ok = ok and all(report["checks"].values())
    report["passed"] = ok
    (out / "report.json").write_text(json.dumps(report, indent=2))
    print(json.dumps(report, indent=2))
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main(sys.argv[1], sys.argv[2]))
