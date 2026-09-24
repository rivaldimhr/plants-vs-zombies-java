package entity.plant;

// Penahan yang lebih kuat dan tinggi; tidak bisa dilompati Pole Vaulting Zombie
public class TallNut extends Plant {

    public TallNut(int x, int y) {
        super("Tall-nut", 2000, false, 0, 0, 125, 0, 30, x, y, "tallnut");
    }

    @Override
    protected String getSpriteId() {
        double ratio = (double) health / maxHealth;
        if (ratio > 2.0 / 3) {
            return "tallnut";
        } else if (ratio > 1.0 / 3) {
            return "tallnut_cracked1";
        }
        return "tallnut_cracked2";
    }

    @Override
    protected int drawMaxHeight() {
        return 76;
    }
}
