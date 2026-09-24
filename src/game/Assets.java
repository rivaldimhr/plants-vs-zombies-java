package game;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.ImageIcon;

/**
 * Tempat load semua gambar. Gambar cuma di-load sekali lalu disimpan di cache,
 * jadi aman dipanggil tiap frame. Path relatif terhadap root project
 * (contoh: "image/IMAGE/MENU.png"), jadi game harus dijalankan dari root project.
 */
public final class Assets {
    private static final Map<String, Image> cache = new ConcurrentHashMap<>();
    private static final Set<String> missing = ConcurrentHashMap.newKeySet();
    private static volatile ImageObserver observer;

    private Assets() {
    }

    // Komponen yang di-repaint saat frame GIF berikutnya siap (supaya GIF beranimasi)
    public static void setObserver(ImageObserver imageObserver) {
        observer = imageObserver;
    }

    public static ImageObserver observer() {
        return observer;
    }

    // Return null kalau file tidak ada (drawImage dengan null tidak menggambar apa-apa)
    public static Image get(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        Image cached = cache.get(path);
        if (cached != null) {
            return cached;
        }
        if (!new File(path).isFile()) {
            if (missing.add(path)) {
                System.err.println("Gambar tidak ditemukan: " + path);
            }
            return null;
        }
        // ImageIcon dipakai (bukan ImageIO) supaya GIF tetap animasi
        Image img = new ImageIcon(path).getImage();
        cache.put(path, img);
        return img;
    }
}
