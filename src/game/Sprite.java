package game;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Animasi berupa deretan frame dengan durasi yang sama.
 * Waktu animasi dipegang oleh pemakai (entity), jadi tiap zombie bisa berada di
 * frame yang berbeda, animasi berhenti saat pause, dan ikut cepat saat 2x.
 */
public final class Sprite {
    public static final Sprite EMPTY = new Sprite(
            new BufferedImage[] { new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB) }, 1000);

    private final BufferedImage[] frames;
    private final int frameMs;
    private final Map<Integer, BufferedImage[]> tints = new ConcurrentHashMap<>();
    private final Map<Long, Sprite> scaled = new ConcurrentHashMap<>();

    public Sprite(BufferedImage[] frames, int frameMs) {
        this.frames = frames;
        this.frameMs = Math.max(1, frameMs);
    }

    // Potong sprite sheet (frame berjajar horizontal) menjadi frame-frame
    public static Sprite fromSheet(BufferedImage sheet, int frameCount, int frameMs) {
        int count = Math.max(1, frameCount);
        int w = sheet.getWidth() / count;
        BufferedImage[] frames = new BufferedImage[count];
        for (int i = 0; i < count; i++) {
            // salin (bukan subimage) supaya tiap frame punya buffer sendiri yang cepat digambar
            BufferedImage frame = new BufferedImage(w, sheet.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
            java.awt.Graphics2D g = frame.createGraphics();
            g.drawImage(sheet, -i * w, 0, null);
            g.dispose();
            frames[i] = frame;
        }
        return new Sprite(frames, frameMs);
    }

    public int getFrameCount() {
        return frames.length;
    }

    public int getFrameMs() {
        return frameMs;
    }

    public long getDurationMs() {
        return (long) frames.length * frameMs;
    }

    public int getWidth() {
        return frames[0].getWidth();
    }

    public int getHeight() {
        return frames[0].getHeight();
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    /**
     * Sprite yang sama dengan ukuran frame tertentu (di-cache). Menggambar gambar yang
     * sudah pas ukurannya jauh lebih cepat daripada memperkecilnya setiap frame.
     */
    public Sprite scaledTo(int width, int height) {
        if (isEmpty() || (width == getWidth() && height == getHeight()) || width <= 0 || height <= 0) {
            return this;
        }
        long key = ((long) width << 32) | height;
        return scaled.computeIfAbsent(key, k -> {
            BufferedImage[] resized = new BufferedImage[frames.length];
            for (int i = 0; i < frames.length; i++) {
                resized[i] = Assets.resize(frames[i], width, height);
            }
            return new Sprite(resized, frameMs);
        });
    }

    public int frameIndex(long timeMs, boolean loop) {
        long index = Math.max(0, timeMs) / frameMs; // long: waktu bisa sangat besar (currentTimeMillis)
        return (int) (loop ? index % frames.length : Math.min(index, frames.length - 1));
    }

    public BufferedImage frame(int index) {
        return frames[index];
    }

    public BufferedImage frameAt(long timeMs, boolean loop) {
        return frames[frameIndex(timeMs, loop)];
    }

    /**
     * Versi frame yang diwarnai satu warna (siluet): putih untuk kilat saat terkena
     * tembakan, biru saat diperlambat, hitam untuk zombie yang gosong.
     * Alpha warna menentukan kekuatan tint. Hasilnya di-cache.
     */
    public BufferedImage tinted(int index, Color color) {
        BufferedImage[] cache = tints.computeIfAbsent(color.getRGB(), k -> new BufferedImage[frames.length]);
        BufferedImage result = cache[index];
        if (result == null) {
            result = tint(frames[index], color);
            cache[index] = result;
        }
        return result;
    }

    private static BufferedImage tint(BufferedImage src, Color color) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        // getRGB/setRGB bekerja dengan warna non-premultiplied; hasil dikonversi di akhir
        int rgb = color.getRGB() & 0xFFFFFF;
        int strength = color.getAlpha();
        int[] pixels = src.getRGB(0, 0, w, h, null, 0, w);
        for (int i = 0; i < pixels.length; i++) {
            int alpha = (pixels[i] >>> 24) * strength / 255;
            pixels[i] = (alpha << 24) | rgb;
        }
        out.setRGB(0, 0, w, h, pixels, 0, w);
        return Assets.toFastFormat(out);
    }
}
