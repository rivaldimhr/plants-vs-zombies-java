"""
Perkecil PNG di dalam folder build (bukan file asli di repo): ubah ke palet <=256 warna dengan transparansi,
hanya bila hasilnya lebih kecil dan tidak berbeda nyata dari aslinya (selisih rata-rata kecil).

  python3 tools/compress_images.py build/classes/image
"""
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageStat

MAX_MEAN_DIFF = 3.0  # rata-rata selisih yang terlihat per kanal (0–255); di atas ini gambar asli dipertahankan


def visible_diff(a, b):
    """Selisih yang terlihat: tempel kedua gambar di atas latar abu-abu (warna piksel transparan tidak dihitung)."""
    bg = Image.new("RGBA", a.size, (128, 128, 128, 255))
    ca, cb = Image.alpha_composite(bg, a), Image.alpha_composite(bg, b)
    return max(ImageStat.Stat(ImageChops.difference(ca.convert("RGB"), cb.convert("RGB"))).mean)


def compress(path: Path):
    src = Image.open(path)
    src.load()
    rgba = src.convert("RGBA")
    q = rgba.quantize(colors=256, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.NONE)
    back = q.convert("RGBA")
    diff = visible_diff(rgba, back)
    if diff > MAX_MEAN_DIFF:
        return 0, "dipertahankan (beda %.1f)" % diff
    tmp = path.with_suffix(".tmp.png")
    q.save(tmp, optimize=True)
    old, new = path.stat().st_size, tmp.stat().st_size
    if new < old:
        tmp.replace(path)
        return old - new, ""
    tmp.unlink()
    return 0, "tidak lebih kecil"


def main(root):
    files = sorted(Path(root).rglob("*.png"))
    before = sum(f.stat().st_size for f in files)
    kept = 0
    for f in files:
        saved, note = compress(f)
        if note.startswith("dipertahankan"):
            kept += 1
    after = sum(f.stat().st_size for f in files)
    print(f"PNG: {before / 1e6:.1f} MB -> {after / 1e6:.1f} MB ({len(files)} file, {kept} dipertahankan karena warnanya terlalu banyak)")


if __name__ == "__main__":
    main(sys.argv[1])
