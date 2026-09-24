"""
Membuat versi sprite yang siap dipakai game di image/sprites/.

Gambar asli banyak yang kanvasnya jauh lebih besar dari karakternya (misalnya
Dolphinride.gif 720x693 dengan zombie hanya di tengah), sehingga di tile 60 px
karakternya jadi kecil. Selain itu decoder GIF Java menggambar kotak hitam pada
beberapa GIF (disposal "restore to background").

Script ini, untuk setiap gambar:
  1. menggabungkan semua frame (untuk GIF) menjadi RGBA penuh,
  2. memotong area kosong (bounding box gabungan semua frame). Kalau karakter di
     GIF berpindah posisi (misalnya TheAdvancing_zombie.gif), tiap frame dipotong
     sendiri lalu diratakan tengah-bawah supaya karakter "jalan di tempat",
  3. mengecilkan agar sisi terpanjang maksimal MAX_SIZE px,
  4. menyimpan ulang: PNG biasa, atau GIF dengan transparansi & disposal yang
     aman untuk Java.

File asli di image/ tidak diubah. Jalankan dari root project:
    python tools/prepare_sprites.py
Butuh Pillow (pip install pillow).
"""
import os

from PIL import Image, ImageSequence

SRC_DIR = "image"
OUT_DIR = os.path.join("image", "sprites")
MAX_SIZE = 120
PADDING = 2
# kalau lebar gabungan > 1.5x lebar frame terbesar, anggap karakternya berpindah posisi
MOVING_RATIO = 1.5
RESAMPLE = getattr(Image, "Resampling", Image).LANCZOS

SPRITES = [
    # plants
    "peashooter.gif", "repeater.gif", "snowpea.png", "PuffShroom.png", "Sunflower.gif",
    "Wallnut1.png", "Wallnut_cracked1.png", "Wallnut_cracked2.png",
    "TallNut1.gif", "Tallnut2.gif", "Tallnut3.gif",
    "lily_pad.gif", "Squash.gif", "TangleKelp.gif",
    # zombies
    "TheAdvancing_zombie.gif", "Conehead_Zombie.gif", "Bucketwalk.gif", "Running_Zombie_football.gif",
    "Transparent_newspaper_idle.gif", "Polerun.gif", "Duckytube.png", "ConeDuckyTube.png",
    "Transparent_snorkel_zombie_idle.gif", "Dolphinride.gif",
    # projectiles & hud
    "Pea.png", "ProjectileSnowPea.png", "PuffShroom_puff1.png", "sun.gif", "shovel.png",
]


def load_frames(path):
    im = Image.open(path)
    frames, durations = [], []
    for frame in ImageSequence.Iterator(im):
        frames.append(frame.convert("RGBA"))
        durations.append(frame.info.get("duration", im.info.get("duration", 60)) or 60)
    return frames, durations


def union_bbox(frames):
    boxes = [f.getbbox() for f in frames if f.getbbox()]
    if not boxes:
        return (0, 0) + frames[0].size
    left = max(min(b[0] for b in boxes) - PADDING, 0)
    top = max(min(b[1] for b in boxes) - PADDING, 0)
    right = min(max(b[2] for b in boxes) + PADDING, frames[0].width)
    bottom = min(max(b[3] for b in boxes) + PADDING, frames[0].height)
    return left, top, right, bottom


def crop_in_place(frames):
    boxes = [f.getbbox() or (0, 0, 1, 1) for f in frames]
    w = max(b[2] - b[0] for b in boxes) + 2 * PADDING
    h = max(b[3] - b[1] for b in boxes) + 2 * PADDING
    result = []
    for f, b in zip(frames, boxes):
        canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        part = f.crop(b)
        canvas.paste(part, ((w - part.width) // 2, h - PADDING - part.height))
        result.append(canvas)
    return result


def is_moving(frames):
    boxes = [f.getbbox() for f in frames if f.getbbox()]
    if len(boxes) < 2:
        return False
    union_w = max(b[2] for b in boxes) - min(b[0] for b in boxes)
    frame_w = max(b[2] - b[0] for b in boxes)
    return union_w > MOVING_RATIO * frame_w


def to_palette(rgba):
    # index 255 = transparan, 0..254 = warna
    alpha = rgba.getchannel("A")
    p = rgba.convert("RGB").quantize(colors=255, method=Image.Quantize.MEDIANCUT)
    palette = p.getpalette()[: 255 * 3]
    palette += [0] * (256 * 3 - len(palette))
    p.putpalette(palette)
    mask = alpha.point(lambda a: 255 if a < 128 else 0)
    p.paste(255, mask=mask)
    return p


def process(name):
    src = os.path.join(SRC_DIR, name)
    frames, durations = load_frames(src)
    if is_moving(frames):
        frames = crop_in_place(frames)
    else:
        box = union_bbox(frames)
        frames = [f.crop(box) for f in frames]
    scale = min(1.0, MAX_SIZE / max(frames[0].size))
    if scale < 1.0:
        size = (max(1, round(frames[0].width * scale)), max(1, round(frames[0].height * scale)))
        frames = [f.resize(size, RESAMPLE) for f in frames]

    out = os.path.join(OUT_DIR, name)
    if len(frames) == 1:
        if name.lower().endswith(".gif"):
            out = out[:-4] + ".png"
        frames[0].save(out)
    else:
        pal = [to_palette(f) for f in frames]
        pal[0].save(out, save_all=True, append_images=pal[1:], duration=durations, loop=0,
                    disposal=2, transparency=255, optimize=False)
    print(f"{src} {Image.open(src).size} -> {out} {frames[0].size} ({len(frames)} frame)")


def main():
    os.makedirs(OUT_DIR, exist_ok=True)
    for name in SPRITES:
        process(name)


if __name__ == "__main__":
    main()
