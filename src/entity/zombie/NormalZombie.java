package entity.zombie;

// Zombie biasa, hanya di darat
public class NormalZombie extends Zombie {

    public NormalZombie(int x, int y) {
        super("Normal Zombie", 125, false, 100, 1, x, y, "image/sprites/TheAdvancing_zombie.gif");
    }

}
