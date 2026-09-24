package entity.plant;

// Penahan: tidak menyerang, gambarnya retak sesuai sisa HP
public class WallNut extends Plant {

    public WallNut(int x, int y) {
        super("Wall-nut", 1000, false, 0, 0, 50, 0, 20, x, y, "wallnut");
    }

    @Override
    protected String getSpriteId() {
        double ratio = (double) health / maxHealth;
        if (ratio > 2.0 / 3) {
            return "wallnut";
        } else if (ratio > 1.0 / 3) {
            return "wallnut_cracked1";
        }
        return "wallnut_cracked2";
    }
}
