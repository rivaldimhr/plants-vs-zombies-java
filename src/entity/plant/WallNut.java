package entity.plant;

// Penahan: tidak menyerang, gambarnya retak sesuai sisa HP
public class WallNut extends Plant {

    public WallNut(int x, int y) {
        super("Wall-nut", 1000, false, 0, 0, 50, 0, 20, x, y, "image/sprites/Wallnut1.png");
    }

    @Override
    protected String getImagePath() {
        double ratio = (double) health / maxHealth;
        if (ratio > 2.0 / 3) {
            return "image/sprites/Wallnut1.png";
        } else if (ratio > 1.0 / 3) {
            return "image/sprites/Wallnut_cracked1.png";
        }
        return "image/sprites/Wallnut_cracked2.png";
    }
}
