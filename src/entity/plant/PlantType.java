package entity.plant;

import java.util.function.BiFunction;

/**
 * Factory semua jenis tanaman. Urutan enum = urutan kartu di Inventory & Almanac.
 * Contoh: {@code Plant p = PlantType.PEASHOOTER.create(x, y);}
 */
public enum PlantType {
    PEASHOOTER(Peashooter::new, "Menembakkan kacang ke zombie di barisnya."),
    SUNFLOWER(Sunflower::new, "Menghasilkan sun. Klik sun yang muncul untuk mengambilnya."),
    CHERRY_BOMB(CherryBomb::new, "Meledak dan menghancurkan semua zombie di area 3x3."),
    WALL_NUT(WallNut::new, "Kulit keras untuk menahan zombie."),
    POTATO_MINE(PotatoMine::new, "Ranjau murah. Butuh waktu untuk aktif, lalu meledak saat diinjak zombie."),
    SNOW_PEA(SnowPea::new, "Menembakkan kacang es yang memperlambat zombie."),
    SQUASH(Squash::new, "Melompat dan menghantam zombie terdekat sampai gepeng."),
    REPEATER(Repeater::new, "Menembakkan dua kacang sekaligus."),
    LILY_PAD(LilyPad::new, "Mengapung di kolam. Tanaman darat bisa ditanam di atasnya."),
    TANGLE_KELP(TangleKelp::new, "Tanaman air yang menarik zombie ke bawah air."),
    PUFF_SHROOM(PuffShroom::new, "Jamur gratis yang menembakkan spora jarak pendek. Tidur saat siang."),
    SUN_SHROOM(SunShroom::new, "Jamur penghasil sun untuk malam hari. Tumbuh besar setelah 1 menit."),
    FUME_SHROOM(FumeShroom::new, "Semburan asapnya menembus semua zombie dalam 4 tile. Tidur saat siang."),
    TALL_NUT(TallNut::new, "Penahan super kuat yang tidak bisa dilompati zombie."),
    JALAPENO(Jalapeno::new, "Membakar semua zombie di satu baris.");

    private final BiFunction<Integer, Integer, Plant> factory;
    private final String description;
    private final Plant prototype; // untuk membaca nama/cost/cooldown tanpa menanam

    PlantType(BiFunction<Integer, Integer, Plant> factory, String description) {
        this.factory = factory;
        this.description = description;
        this.prototype = factory.apply(0, 0);
    }

    public Plant create(int x, int y) {
        return factory.apply(x, y);
    }

    public String getCardImage() {
        return "image/cards/" + name() + ".png";
    }

    public String getDescription() {
        return description;
    }

    public String getDisplayName() {
        return prototype.getName();
    }

    public int getCost() {
        return prototype.getCost();
    }

    public int getCooldown() {
        return prototype.getCooldown();
    }

    public boolean isAquatic() {
        return prototype.isAquatic();
    }

    public boolean isNocturnal() {
        return prototype.isNocturnal();
    }

    // Prototipe hanya untuk dibaca (stat, sprite di Almanac), jangan ditanam
    public Plant getPrototype() {
        return prototype;
    }
}
