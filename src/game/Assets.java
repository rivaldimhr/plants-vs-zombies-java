package game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

/**
 * Tempat load semua aset (gambar, sprite, suara). Aset dicari di classpath dulu
 * (saat dijalankan dari JAR), lalu di folder project (saat dijalankan dari source).
 * Semua hasil load di-cache, jadi aman dipanggil tiap frame.
 */
public final class Assets {
    private static final String SPRITE_DIR = "image/sprites/";
    private static final Map<String, BufferedImage> images = new ConcurrentHashMap<>();
    private static final Map<String, BufferedImage> scaledImages = new ConcurrentHashMap<>();
    private static final Map<String, Sprite> sprites = new ConcurrentHashMap<>();
    private static final Set<String> missing = ConcurrentHashMap.newKeySet();
    private static volatile Properties spriteManifest;

    private Assets() {
    }

    // Lokasi aset: dari JAR/classpath kalau ada, kalau tidak dari folder project
    public static URL resource(String path) {
        URL url = Assets.class.getResource("/" + path);
        if (url != null) {
            return url;
        }
        File file = new File(path);
        if (file.isFile()) {
            try {
                return file.toURI().toURL();
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    static void warnMissing(String path) {
        if (missing.add(path)) {
            System.err.println("Aset tidak ditemukan: " + path);
        }
    }

    // Gambar statis (background, kartu, UI). Return null kalau tidak ada.
    public static BufferedImage get(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        BufferedImage cached = images.get(path);
        if (cached != null) {
            return cached;
        }
        URL url = resource(path);
        if (url == null) {
            warnMissing(path);
            return null;
        }
        try {
            BufferedImage image = ImageIO.read(url);
            if (image != null) {
                image = toFastFormat(image);
                images.put(path, image);
            }
            return image;
        } catch (IOException e) {
            warnMissing(path);
            return null;
        }
    }

    /*
     * Gambar yang sudah diperkecil ke ukuran tertentu (di-cache). Memperkecil gambar
     * besar (misalnya seed bank 1166x204) setiap frame itu mahal; cukup sekali saja.
     */
    public static BufferedImage get(String path, int width, int height) {
        BufferedImage source = get(path);
        if (source == null || (source.getWidth() == width && source.getHeight() == height)) {
            return source;
        }
        return scaledImages.computeIfAbsent(path + "@" + width + "x" + height, key -> resize(source, width, height));
    }

    public static BufferedImage resize(BufferedImage src, int width, int height) {
        BufferedImage current = src;
        // perkecil bertahap (maks. setengah tiap langkah) supaya hasilnya halus
        while (current.getWidth() / 2 >= width && current.getHeight() / 2 >= height) {
            current = step(current, current.getWidth() / 2, current.getHeight() / 2);
        }
        return step(current, width, height);
    }

    private static BufferedImage step(BufferedImage src, int width, int height) {
        BufferedImage out = new BufferedImage(Math.max(1, width), Math.max(1, height), BufferedImage.TYPE_INT_ARGB_PRE);
        java.awt.Graphics2D g = out.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(java.awt.RenderingHints.KEY_RENDERING, java.awt.RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return out;
    }

    /*
     * ImageIO menghasilkan format piksel yang lambat digambar Java2D (misalnya
     * 4BYTE_ABGR). Dikonversi sekali ke INT_ARGB_PRE supaya render jauh lebih cepat.
     */
    public static BufferedImage toFastFormat(BufferedImage src) {
        int type = isOpaque(src) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB_PRE;
        if (src.getType() == type) {
            return src;
        }
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), type);
        java.awt.Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    // Gambar tanpa piksel transparan (background) disimpan sebagai INT_RGB yang lebih cepat
    private static boolean isOpaque(BufferedImage image) {
        if (!image.getColorModel().hasAlpha()) {
            return true;
        }
        int w = image.getWidth();
        int[] row = new int[w];
        for (int y = 0; y < image.getHeight(); y++) {
            image.getRGB(0, y, w, 1, row, 0, w);
            for (int pixel : row) {
                if ((pixel >>> 24) != 0xFF) {
                    return false;
                }
            }
        }
        return true;
    }

    // Sprite animasi dari image/sprites/<id>.png (dibuat oleh tools/build_assets.py)
    public static Sprite sprite(String id) {
        if (id == null) {
            return Sprite.EMPTY;
        }
        return sprites.computeIfAbsent(id, Assets::loadSprite);
    }

    private static Sprite loadSprite(String id) {
        BufferedImage sheet = get(SPRITE_DIR + id + ".png");
        if (sheet == null) {
            return Sprite.EMPTY;
        }
        String[] parts = manifest().getProperty(id, "1,1000").split(",");
        return Sprite.fromSheet(sheet, Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
    }

    private static Properties manifest() {
        Properties result = spriteManifest;
        if (result == null) {
            result = new Properties();
            URL url = resource(SPRITE_DIR + "sprites.properties");
            if (url == null) {
                warnMissing(SPRITE_DIR + "sprites.properties");
            } else {
                try (InputStream in = url.openStream()) {
                    result.load(in);
                } catch (IOException e) {
                    warnMissing(SPRITE_DIR + "sprites.properties");
                }
            }
            spriteManifest = result;
        }
        return result;
    }
}
