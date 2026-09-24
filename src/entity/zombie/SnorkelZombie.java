package entity.zombie;

// Menyelam di kolam (tidak bisa kena peluru) dan baru muncul ke permukaan saat menemukan tanaman
public class SnorkelZombie extends Zombie {

    public SnorkelZombie(int x, int y) {
        super("Snorkel Zombie", 100, true, 100, 1, x, y, "image/sprites/Transparent_snorkel_zombie_idle.gif");
    }

    @Override
    public boolean isTargetable() {
        return isEating();
    }
}
