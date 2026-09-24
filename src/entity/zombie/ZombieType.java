package entity.zombie;

import java.util.function.BiFunction;

/**
 * Factory semua jenis zombie. points = "biaya" zombie saat menyusun wave
 * (zombie kuat lebih mahal, jadi lebih jarang muncul di wave kecil).
 */
public enum ZombieType {
    NORMAL(NormalZombie::new, 1, "Zombie biasa. Pelan tapi tidak pernah menyerah."),
    CONEHEAD(ConeheadZombie::new, 2, "Traffic cone di kepalanya membuatnya dua kali lebih tahan."),
    POLE_VAULTING(PoleVaultingZombie::new, 2, "Berlari dan melompati tanaman pertama dengan galah. Tidak bisa melompati Tall-nut."),
    BUCKETHEAD(BucketheadZombie::new, 4, "Ember besi di kepalanya sangat kuat."),
    NEWSPAPER(NewspaperZombie::new, 2, "Terlindung koran. Kalau korannya hancur, dia marah dan jadi cepat."),
    FOOTBALL(FootballZombie::new, 4, "Berlari cepat dengan helm dan baju rugby."),
    DUCKY_TUBE(DuckyTubeZombie::new, 1, "Zombie biasa dengan pelampung bebek. Muncul di kolam."),
    DUCKY_TUBE_CONEHEAD(DuckyTubeConeheadZombie::new, 2, "Conehead dengan pelampung bebek. Muncul di kolam."),
    SNORKEL(SnorkelZombie::new, 2, "Menyelam sehingga tidak bisa ditembak, lalu muncul untuk memakan tanaman."),
    DOLPHIN_RIDER(DolphinRiderZombie::new, 3, "Menunggang lumba-lumba. Tanaman pertama yang ditemui langsung mati.");

    private final BiFunction<Integer, Integer, Zombie> factory;
    private final int points;
    private final String description;
    private final Zombie prototype;

    ZombieType(BiFunction<Integer, Integer, Zombie> factory, int points, String description) {
        this.factory = factory;
        this.points = points;
        this.description = description;
        this.prototype = factory.apply(0, 0);
    }

    public Zombie create(int x, int y) {
        return factory.apply(x, y);
    }

    public boolean isAquatic() {
        return prototype.isAquatic();
    }

    public String getDisplayName() {
        return prototype.getName();
    }

    public int getPoints() {
        return points;
    }

    public String getDescription() {
        return description;
    }

    // Prototipe hanya untuk dibaca (stat, sprite di Almanac)
    public Zombie getPrototype() {
        return prototype;
    }
}
