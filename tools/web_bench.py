"""
Ukur kinerja versi browser untuk beberapa pengaturan (dipakai memilih pengaturan default).

Untuk tiap konfigurasi (browser baru, tanpa cache):
  - detik sampai menu tampil,
  - jeda klik: START -> layar pilih level, Level 1 -> inventory, PLAY -> permainan (ms sampai layar berubah),
  - ups/fps game selama ~15 detik di dalam permainan (dari log PVZ_PERF).

  python tools/web_bench.py dist/web dist/web/check/bench.json
"""
import json
import re
import shutil
import sys
import tempfile
import time
from pathlib import Path

from PIL import Image, ImageChops, ImageStat
from playwright.sync_api import sync_playwright

sys.path.insert(0, str(Path(__file__).parent))
from serve import make_server  # noqa: E402

CONFIGS = {
    "tanpa preload (seperti sebelumnya)": "fps=120&fast=0&scale=1.5&preload=0",
    "preload, 120 fps, kualitas tinggi": "fps=120&fast=0&scale=1.5",
    "preload, 60 fps, render cepat": "fps=60&fast=1&scale=1.5",
    "preload, 40 fps, render cepat": "fps=40&fast=1&scale=1.5",
}
MENU_START, LEVEL_1, PLAY = (328, 322), (123, 120), (607, 22)
CARD_PEASHOOTER = (69, 35)   # slot deck ke-2 di permainan
WAIT_AFTER_MENU = 8.0        # waktu "membaca menu" sebelum klik START (memberi kesempatan preload)
PICKS = [(208, 185), (124, 185), (376, 185), (292, 185), (460, 185), (208, 265)]


def shot(stage, path):
    stage.screenshot(path=path)
    return Image.open(path).convert("L")


def click_latency(page, stage, pos, scale, tmp, timeout=20.0):
    before = shot(stage, tmp)
    stage.click(position={"x": pos[0] * scale, "y": pos[1] * scale})
    t0 = time.time()
    while time.time() - t0 < timeout:
        now = shot(stage, tmp)
        if ImageStat.Stat(ImageChops.difference(before, now)).mean[0] > 10:
            return round((time.time() - t0) * 1000)
        page.wait_for_timeout(50)
    return None


def run(browser, base, name, qs):
    scale = float(re.search(r"scale=([\d.]+)", qs).group(1))
    ctx = browser.new_context(viewport={"width": 1280, "height": 900})
    page = ctx.new_page()
    perf = []
    page.on("console", lambda m: "PVZ_PERF" in m.text and perf.append(m.text.strip()))
    t0 = time.time()
    page.goto(f"{base}?autoplay=1&perf=1&{qs}")
    res = {"config": name, "query": qs}
    try:
        page.wait_for_function("window.__pvzReady === true", timeout=240_000)
    except Exception:
        res["error"] = "tidak termuat"
        ctx.close()
        return res
    res["detik_sampai_menu"] = round(time.time() - t0, 1)
    page.wait_for_timeout(int(WAIT_AFTER_MENU * 1000))
    stage = page.locator("#stage")
    tmp = tempfile.mktemp(suffix=".png")
    res["klik_start_ms"] = click_latency(page, stage, MENU_START, scale, tmp)
    page.wait_for_timeout(800)
    res["klik_level_ms"] = click_latency(page, stage, LEVEL_1, scale, tmp)
    page.wait_for_timeout(800)
    for p in PICKS:
        stage.click(position={"x": p[0] * scale, "y": p[1] * scale})
        page.wait_for_timeout(250)
    res["klik_play_ms"] = click_latency(page, stage, PLAY, scale, tmp)
    page.wait_for_timeout(4000)  # intro Ready-Set-Plant
    res["klik_kartu_ms"] = click_latency(page, stage, CARD_PEASHOOTER, scale, tmp)
    n = len(perf)
    page.wait_for_timeout(16_000)
    game = [l for l in perf[n:] if "GAME_LEVEL" in l]
    vals = [tuple(map(float, re.search(r"ups=([\d.]+) fps=([\d.]+)", l).groups())) for l in game]
    if vals:
        res["ups_saat_main"] = round(sum(v[0] for v in vals) / len(vals), 1)
        res["fps_saat_main"] = round(sum(v[1] for v in vals) / len(vals), 1)
    text = page.evaluate("(document.getElementById('console') || {}).textContent || ''")
    lines = [l for l in text.splitlines() if l.startswith("PVZ_")] + perf
    if not vals:
        game = [l for l in lines if "GAME_LEVEL" in l]
        vals = [tuple(map(float, re.search(r"ups=([\d.]+) fps=([\d.]+)", l).groups())) for l in game]
        if vals:
            res["ups_saat_main"] = round(sum(v[0] for v in vals) / len(vals), 1)
            res["fps_saat_main"] = round(sum(v[1] for v in vals) / len(vals), 1)
    res["preload"] = next((l for l in lines if l.startswith("PVZ_PRELOAD")), None)
    res["log_perf"] = lines[-6:]
    ctx.close()
    return res


def main(site, out):
    root = Path(tempfile.mkdtemp())
    shutil.copytree(site, root / "pvz", ignore=shutil.ignore_patterns("check"))
    srv = make_server(root)
    import threading
    threading.Thread(target=srv.serve_forever, daemon=True).start()
    base = f"http://127.0.0.1:{srv.server_address[1]}/pvz/"
    results = []
    with sync_playwright() as p:
        b = p.chromium.launch()
        for name, qs in CONFIGS.items():
            r = run(b, base, name, qs)
            print(json.dumps(r, ensure_ascii=False))
            results.append(r)
        b.close()
    srv.shutdown()
    Path(out).parent.mkdir(parents=True, exist_ok=True)
    Path(out).write_text(json.dumps(results, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
