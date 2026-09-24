package entity.plant;

import java.util.function.BiFunction;

/**
 * Factory semua jenis tanaman. Urutan enum = urutan kartu di layar Inventory.
 * Contoh: {@code Plant p = PlantType.PEASHOOTER.create(x, y);}
 */
public enum PlantType {
    PEASHOOTER("image/IMAGE/Peashooter.png", Peashooter::new),
    TALL_NUT("image/IMAGE/Tall-Nut.png", TallNut::new),
    PUFF_SHROOM("image/IMAGE/Puff-Shroom.png", PuffShroom::new),
    SQUASH("image/IMAGE/Squash.png", Squash::new),
    LILY_PAD("image/IMAGE/Lily Pad.png", LilyPad::new),
    REPEATER("image/IMAGE/Repeater.png", Repeater::new),
    TANGLE_KELP("image/IMAGE/Tangle Kelp.png", TangleKelp::new),
    SNOW_PEA("image/IMAGE/Snow Pea.png", SnowPea::new),
    WALL_NUT("image/IMAGE/Wall-Nut.png", WallNut::new),
    SUNFLOWER("image/IMAGE/Sunflower.png", Sunflower::new);

    private final String cardImage;
    private final BiFunction<Integer, Integer, Plant> factory;
    private final Plant prototype; // untuk membaca nama/cost/cooldown tanpa menanam

    PlantType(String cardImage, BiFunction<Integer, Integer, Plant> factory) {
        this.cardImage = cardImage;
        this.factory = factory;
        this.prototype = factory.apply(0, 0);
    }

    public Plant create(int x, int y) {
        return factory.apply(x, y);
    }

    public String getCardImage() {
        return cardImage;
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
}
