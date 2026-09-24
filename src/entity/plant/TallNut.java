package entity.plant;

// Penahan yang lebih kuat; tidak bisa dilompati Pole Vaulting Zombie
public class TallNut extends Plant {

    public TallNut(int x, int y) {
        super("Tall-nut", 2000, false, 0, 0, 125, 0, 30, x, y, "image/sprites/TallNut1.gif");
    }

    @Override
    protected String getImagePath() {
        double ratio = (double) health / maxHealth;
        if (ratio > 2.0 / 3) {
            return "image/sprites/TallNut1.gif";
        } else if (ratio > 1.0 / 3) {
            return "image/sprites/Tallnut2.gif";
        }
        return "image/sprites/Tallnut3.gif";
    }
}
