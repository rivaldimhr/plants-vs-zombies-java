"""
Membangun semua aset turunan yang dipakai game dari gambar asli di image/.

Output:
  image/sprites/<id>.png          sprite sheet: semua frame berjajar horizontal
  image/sprites/sprites.properties manifest: <id>=<jumlah frame>,<durasi per frame ms>
  image/cards/<PLANT_TYPE>.png    kartu seed packet (144x201) untuk semua tanaman
  image/ui/*                      aset UI (seed bank, counter, layar game over, dll.)

Kenapa sprite sheet, bukan GIF langsung?
  - GIF di Java beranimasi sendiri (tidak ikut pause / kecepatan 2x) dan semua
    zombie berbagi frame yang sama.
  - Dengan sprite sheet, tiap entity punya waktu animasinya sendiri, animasi
    bisa diputar sekali dari awal (misalnya animasi mati), dan bisa diberi
    efek (kilat putih saat terkena tembakan, warna biru saat diperlambat).

Tanaman baru (Cherry Bomb, Potato Mine, Jalapeno) digambar dengan kode di
script ini; Sun-shroom dan Fume-shroom adalah recolor Puff-shroom.

Jalankan dari root project:  python tools/build_assets.py
Butuh Pillow dan numpy (pip install pillow numpy).
"""
import math
import os
import shutil

import numpy as np
from PIL import Image, ImageDraw, ImageFont, ImageSequence

SRC = "image"
SPRITES = os.path.join("image", "sprites")
CARDS = os.path.join("image", "cards")
UI = os.path.join("image", "ui")

MAX_SIZE = 100  # sisi terpanjang frame (px)
MAX_FRAMES = 64
PADDING = 2
MOVING_RATIO = 1.5
RESAMPLE = getattr(Image, "Resampling", Image).LANCZOS
BICUBIC = getattr(Image, "Resampling", Image).BICUBIC

manifest = {}


# ---------------------------------------------------------------- util frame

def load_frames(path):
    im = Image.open(path)
    frames, durations = [], []
    for frame in ImageSequence.Iterator(im):
        frames.append(frame.convert("RGBA"))
        durations.append(frame.info.get("duration", im.info.get("duration", 80)) or 80)
    return frames, durations


def union_bbox(frames):
    boxes = [f.getbbox() for f in frames if f.getbbox()]
    if not boxes:
        return (0, 0) + frames[0].size
    return (max(min(b[0] for b in boxes) - PADDING, 0), max(min(b[1] for b in boxes) - PADDING, 0),
            min(max(b[2] for b in boxes) + PADDING, frames[0].width),
            min(max(b[3] for b in boxes) + PADDING, frames[0].height))


def is_moving(frames):
    boxes = [f.getbbox() for f in frames if f.getbbox()]
    if len(boxes) < 2:
        return False
    union_w = max(b[2] for b in boxes) - min(b[0] for b in boxes)
    return union_w > MOVING_RATIO * max(b[2] - b[0] for b in boxes)


def crop_in_place(frames):
    """Karakter yang berpindah posisi di GIF: tiap frame dipotong lalu diratakan tengah-bawah."""
    boxes = [f.getbbox() or (0, 0, 1, 1) for f in frames]
    w = max(b[2] - b[0] for b in boxes) + 2 * PADDING
    h = max(b[3] - b[1] for b in boxes) + 2 * PADDING
    out = []
    for f, b in zip(frames, boxes):
        canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        part = f.crop(b)
        canvas.paste(part, ((w - part.width) // 2, h - PADDING - part.height))
        out.append(canvas)
    return out


def fit(frames, max_size=MAX_SIZE):
    scale = min(1.0, max_size / max(frames[0].size))
    if scale >= 1.0:
        return frames
    size = (max(1, round(frames[0].width * scale)), max(1, round(frames[0].height * scale)))
    return [f.resize(size, RESAMPLE) for f in frames]


def subsample(frames, durations):
    total = sum(durations)
    if len(frames) > MAX_FRAMES:
        step = len(frames) / MAX_FRAMES
        frames = [frames[int(i * step)] for i in range(MAX_FRAMES)]
    return frames, max(20, round(total / len(frames)))


def save_sheet(sprite_id, frames, frame_ms):
    w, h = frames[0].size
    sheet = Image.new("RGBA", (w * len(frames), h), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        sheet.paste(f, (i * w, 0))
    sheet.save(os.path.join(SPRITES, sprite_id + ".png"), optimize=True)
    manifest[sprite_id] = (len(frames), frame_ms)
    print(f"  {sprite_id:22s} {len(frames):3d} frame  {w}x{h}  {frame_ms} ms")


def sprite_from_file(sprite_id, name, max_size=MAX_SIZE):
    frames, durations = load_frames(os.path.join(SRC, name))
    frames = crop_in_place(frames) if is_moving(frames) else [f.crop(union_bbox(frames)) for f in frames]
    frames, frame_ms = subsample(fit(frames, max_size), durations)
    save_sheet(sprite_id, frames, frame_ms)
    return frames


# ---------------------------------------------------------------- recolor

def recolor(img, hue_deg, sat_mul=1.0, val_mul=1.0, hue_range=(230, 330)):
    """Ganti hue piksel ungu (hue_range, derajat) menjadi hue_deg."""
    arr = np.array(img.convert("RGBA")).astype(np.float32) / 255.0
    rgb, alpha = arr[..., :3], arr[..., 3:]
    r, g, b = rgb[..., 0], rgb[..., 1], rgb[..., 2]
    mx, mn = rgb.max(-1), rgb.min(-1)
    diff = mx - mn + 1e-6
    hue = np.where(mx == r, (g - b) / diff % 6, np.where(mx == g, (b - r) / diff + 2, (r - g) / diff + 4)) * 60
    sat = np.where(mx > 0, (mx - mn) / (mx + 1e-6), 0)
    val = mx
    mask = (hue >= hue_range[0]) & (hue <= hue_range[1]) & (sat > 0.15)
    hue = np.where(mask, hue_deg, hue)
    sat = np.where(mask, np.clip(sat * sat_mul, 0, 1), sat)
    val = np.where(mask, np.clip(val * val_mul, 0, 1), val)
    c = val * sat
    x = c * (1 - np.abs((hue / 60) % 2 - 1))
    m = val - c
    h6 = (hue // 60).astype(int) % 6
    zeros = np.zeros_like(c)
    lut = [(c, x, zeros), (x, c, zeros), (zeros, c, x), (zeros, x, c), (x, zeros, c), (c, zeros, x)]
    out = np.zeros_like(rgb)
    for i, (rr, gg, bb) in enumerate(lut):
        sel = h6 == i
        out[..., 0] = np.where(sel, rr + m, out[..., 0])
        out[..., 1] = np.where(sel, gg + m, out[..., 1])
        out[..., 2] = np.where(sel, bb + m, out[..., 2])
    result = np.concatenate([out, alpha], -1)
    return Image.fromarray((result * 255).astype(np.uint8), "RGBA")


# ---------------------------------------------------------------- gambar tanaman baru

SS = 4  # supersampling
N = 200  # ukuran kanvas logis


def canvas():
    img = Image.new("RGBA", (N * SS, N * SS), (0, 0, 0, 0))
    return img, ImageDraw.Draw(img)


def s(*v):
    return [int(round(a * SS)) for a in v]


def shaded_ellipse(draw, box, dark, light, outline, width=3, light_offset=(-0.25, -0.3)):
    """Ellipse dengan gradasi radial sederhana (gelap di tepi, terang ke arah highlight)."""
    x0, y0, x1, y1 = box
    cx, cy = (x0 + x1) / 2, (y0 + y1) / 2
    rx, ry = (x1 - x0) / 2, (y1 - y0) / 2
    draw.ellipse(s(x0 - width, y0 - width, x1 + width, y1 + width), fill=outline)
    steps = 24
    for i in range(steps):
        t = i / (steps - 1)
        k = 1 - t * 0.85
        ox = cx + rx * light_offset[0] * t
        oy = cy + ry * light_offset[1] * t
        col = tuple(int(dark[j] + (light[j] - dark[j]) * t) for j in range(3))
        draw.ellipse(s(ox - rx * k, oy - ry * k, ox + rx * k, oy + ry * k), fill=col)


def eyes(draw, cx, cy, spacing=16, size=9, angry=True, brow_color=(20, 10, 10)):
    for side in (-1, 1):
        ex = cx + side * spacing
        draw.ellipse(s(ex - size, cy - size, ex + size, cy + size), fill=(255, 255, 255), outline=(0, 0, 0),
                     width=2 * SS)
        draw.ellipse(s(ex - size * 0.45 + side * 2, cy - size * 0.2, ex + size * 0.45 + side * 2, cy + size * 0.7),
                     fill=(0, 0, 0))
        if angry:
            # alis marah: sisi dalam lebih rendah dari sisi luar
            draw.line(s(ex - side * size * 1.2, cy - size * 0.9, ex + side * size * 1.1, cy - size * 1.8),
                      fill=brow_color, width=5 * SS)


def finish(img, angle=0.0, scale=1.0, dy=0.0):
    img = img.resize((N, N), RESAMPLE)
    if angle or scale != 1.0 or dy:
        w = int(N * scale)
        img2 = img.resize((w, w), RESAMPLE).rotate(angle, resample=BICUBIC, center=(w / 2, w))
        out = Image.new("RGBA", (N, N), (0, 0, 0, 0))
        out.paste(img2, ((N - w) // 2, int(N - w + dy)), img2)
        img = out
    return img


def draw_cherry_bomb():
    img, d = canvas()
    # tangkai
    d.line(s(72, 92, 104, 34), fill=(40, 90, 20), width=7 * SS)
    d.line(s(130, 86, 104, 34), fill=(40, 90, 20), width=7 * SS)
    d.ellipse(s(96, 20, 132, 40), fill=(80, 160, 40), outline=(30, 70, 15), width=2 * SS)
    # buah
    for cx, cy, r in ((70, 128, 44), (132, 120, 46)):
        shaded_ellipse(d, (cx - r, cy - r, cx + r, cy + r), (150, 10, 15), (255, 90, 70), (70, 0, 5))
        d.ellipse(s(cx - r * 0.55, cy - r * 0.7, cx - r * 0.15, cy - r * 0.4), fill=(255, 210, 200))
        eyes(d, cx + 2, cy - 2, spacing=15, size=9)
        d.arc(s(cx - 12, cy + 12, cx + 12, cy + 28), 200, 340, fill=(40, 0, 0), width=4 * SS)
    return img


def draw_potato(armed, light_on=True):
    img, d = canvas()
    if armed:
        # antena
        d.line(s(100, 100, 100, 58), fill=(90, 90, 90), width=5 * SS)
        col = (255, 40, 30) if light_on else (120, 20, 20)
        if light_on:
            d.ellipse(s(84, 34, 116, 66), fill=(255, 120, 90, 90))
        d.ellipse(s(90, 42, 110, 62), fill=col, outline=(60, 0, 0), width=2 * SS)
        shaded_ellipse(d, (38, 92, 162, 178), (140, 95, 45), (215, 170, 105), (80, 50, 20))
        for sx, sy in ((62, 150), (140, 146), (120, 110), (75, 115)):
            d.ellipse(s(sx - 5, sy - 3, sx + 5, sy + 3), fill=(120, 80, 35))
        eyes(d, 100, 128, spacing=17, size=10, angry=False)
        d.arc(s(86, 140, 114, 160), 20, 160, fill=(60, 30, 10), width=4 * SS)
        d.ellipse(s(28, 166, 172, 196), fill=(95, 60, 30))
    else:
        d.line(s(100, 150, 100, 122), fill=(90, 90, 90), width=5 * SS)
        d.ellipse(s(93, 114, 107, 128), fill=(70, 70, 70))
        shaded_ellipse(d, (56, 140, 144, 196), (140, 95, 45), (215, 170, 105), (80, 50, 20))
        d.ellipse(s(20, 162, 180, 198), fill=(95, 60, 30))
        for px, py in ((40, 170), (70, 180), (130, 176), (160, 170)):
            d.ellipse(s(px - 7, py - 4, px + 7, py + 4), fill=(120, 80, 45))
    return img


def draw_jalapeno():
    img, d = canvas()
    body = Image.new("RGBA", (N * SS, N * SS), (0, 0, 0, 0))
    bd = ImageDraw.Draw(body)
    shaded_ellipse(bd, (68, 44, 132, 196), (160, 10, 10), (255, 80, 50), (70, 0, 0), light_offset=(-0.3, -0.2))
    bd.ellipse(s(82, 70, 96, 120), fill=(255, 180, 160))
    eyes(bd, 100, 96, spacing=13, size=8)
    bd.arc(s(88, 116, 112, 134), 200, 340, fill=(50, 0, 0), width=4 * SS)
    body = body.rotate(-12, resample=BICUBIC, center=(100 * SS, 120 * SS))
    img.alpha_composite(body)
    d.polygon(s(78, 52, 122, 48, 116, 36, 84, 38), fill=(60, 140, 40), outline=(25, 70, 15))
    d.line(s(100, 38, 110, 14), fill=(50, 110, 30), width=7 * SS)
    return img


def new_plant_frames():
    print("tanaman baru:")
    cherry = draw_cherry_bomb()
    save_sheet("cherrybomb", fit([finish(cherry, scale=1 + 0.03 * math.sin(i / 8 * 2 * math.pi)) for i in range(8)]),
               110)
    save_sheet("potatomine", fit([finish(draw_potato(True, on)) for on in (True, False)]), 450)
    save_sheet("potatomine_unarmed", fit([finish(draw_potato(False))]), 1000)
    jal = draw_jalapeno()
    save_sheet("jalapeno", fit([finish(jal, angle=4 * math.sin(i / 6 * 2 * math.pi)) for i in range(6)]), 110)

    puff = Image.open(os.path.join(SRC, "PuffShroom.png")).convert("RGBA")
    puff = puff.crop(puff.getbbox())
    sun = recolor(puff, 46, sat_mul=2.2, val_mul=1.45)
    save_sheet("sunshroom", fit([sun]), 1000)
    fume = recolor(puff, 262, sat_mul=0.5, val_mul=0.62)
    save_sheet("fumeshroom", fit([fume]), 1000)
    return {"CHERRY_BOMB": cherry.resize((N, N), RESAMPLE), "POTATO_MINE": draw_potato(True).resize((N, N), RESAMPLE),
            "JALAPENO": jal.resize((N, N), RESAMPLE), "SUN_SHROOM": sun, "FUME_SHROOM": fume}


# ---------------------------------------------------------------- kartu

OLD_CARDS = {
    "PEASHOOTER": "Peashooter", "SUNFLOWER": "Sunflower", "WALL_NUT": "Wall-Nut", "TALL_NUT": "Tall-Nut",
    "SNOW_PEA": "Snow Pea", "SQUASH": "Squash", "LILY_PAD": "Lily Pad", "TANGLE_KELP": "Tangle Kelp",
    "REPEATER": "Repeater", "PUFF_SHROOM": "Puff-Shroom",
}
NEW_CARD_COST = {"CHERRY_BOMB": 150, "POTATO_MINE": 25, "JALAPENO": 125, "SUN_SHROOM": 25, "FUME_SHROOM": 75}


def font(size):
    for name in ("arialbd.ttf", "Arial Bold.ttf", "DejaVuSans-Bold.ttf"):
        try:
            return ImageFont.truetype(name, size)
        except OSError:
            continue
    return ImageFont.load_default()


def make_card(template, art, cost):
    card = template.copy()
    # jendela gambar: isi ulang dengan gradasi kolom tepi supaya tanaman lama hilang
    window = (11, 22, 133, 148)
    col = template.crop((window[0] + 1, window[1], window[0] + 2, window[3]))
    card.paste(col.resize((window[2] - window[0], window[3] - window[1])), window[:2])
    art = art.crop(art.getbbox())
    art.thumbnail((104, 104), RESAMPLE)
    card.alpha_composite(art, ((144 - art.width) // 2, window[3] - art.height - 6))
    # harga
    d = ImageDraw.Draw(card)
    d.rectangle((16, 156, 94, 192), fill=(242, 240, 228))
    f = font(30)
    text = str(cost)
    w = d.textlength(text, font=f)
    d.text((56 - w / 2, 158), text, font=f, fill=(20, 20, 20))
    return card


def build_cards(new_art):
    print("kartu:")
    os.makedirs(CARDS, exist_ok=True)
    for type_name, file in OLD_CARDS.items():
        shutil.copy(os.path.join(SRC, "IMAGE", file + ".png"), os.path.join(CARDS, type_name + ".png"))
    template = Image.open(os.path.join(SRC, "IMAGE", "Peashooter.png")).convert("RGBA")
    for type_name, art in new_art.items():
        make_card(template, art, NEW_CARD_COST[type_name]).save(os.path.join(CARDS, type_name + ".png"))
        print(f"  {type_name}")


# ---------------------------------------------------------------- main

def main():
    for d in (SPRITES, CARDS, UI):
        if os.path.isdir(d):
            shutil.rmtree(d)
        os.makedirs(d)

    print("sprite dari gambar asli:")
    for sprite_id, name in [
        ("peashooter", "peashooter.gif"), ("repeater", "repeater.gif"), ("snowpea", "snowpea.png"),
        ("puffshroom", "PuffShroom.png"), ("sunflower", "Sunflower.gif"), ("wallnut", "Wallnut1.png"),
        ("wallnut_cracked1", "Wallnut_cracked1.png"), ("wallnut_cracked2", "Wallnut_cracked2.png"),
        ("tallnut", "TallNut1.gif"), ("tallnut_cracked1", "Tallnut2.gif"), ("tallnut_cracked2", "Tallnut3.gif"),
        ("lilypad", "lily_pad.gif"), ("squash", "Squash.gif"), ("tanglekelp", "TangleKelp.gif"),
        ("zombie", "TheAdvancing_zombie.gif"), ("conehead", "Conehead_Zombie.gif"), ("buckethead", "Bucketwalk.gif"),
        ("football", "Running_Zombie_football.gif"), ("newspaper", "Transparent_newspaper_idle.gif"),
        ("polevault", "Polerun.gif"), ("duckytube", "Duckytube.png"), ("duckytube_cone", "ConeDuckyTube.png"),
        ("snorkel", "Transparent_snorkel_zombie_idle.gif"), ("dolphin", "Dolphinride.gif"),
        ("zombie_dying", "zombie_normal_dying.gif"), ("football_dying", "zombie_football_dying.gif"),
        ("pea", "Pea.png"), ("snowpea_bullet", "ProjectileSnowPea.png"), ("puff_bullet", "PuffShroom_puff1.png"),
        ("sun", "sun.gif"), ("lawnmower", "lawn_mower.gif"),
    ]:
        sprite_from_file(sprite_id, name)

    # ikon kepala zombie untuk progress bar: frame terakhir animasi mati
    frames, _ = load_frames(os.path.join(SRC, "zombie_normal_dying.gif"))
    head = frames[-1].crop(frames[-1].getbbox())
    save_sheet("zombie_head", fit([head], 40), 1000)

    build_cards(new_plant_frames())

    print("ui:")
    for name in ("deck.png", "Counter.png", "gameOver.jpg", "cursor.png", "huge_wave_of_zombies_text.png",
                 "shovel.png"):
        shutil.copy(os.path.join(SRC, name), os.path.join(UI, name))
        print("  " + name)

    with open(os.path.join(SPRITES, "sprites.properties"), "w", encoding="utf-8") as f:
        f.write("# dibuat oleh tools/build_assets.py: <id>=<jumlah frame>,<durasi per frame (ms)>\n")
        for sprite_id in sorted(manifest):
            frames, ms = manifest[sprite_id]
            f.write(f"{sprite_id}={frames},{ms}\n")


if __name__ == "__main__":
    main()
