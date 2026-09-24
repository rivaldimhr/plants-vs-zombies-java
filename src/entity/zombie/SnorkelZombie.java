package entity.zombie;

import java.awt.Graphics2D;

// Menyelam di kolam (tidak bisa kena peluru) dan baru muncul ke permukaan saat menemukan tanaman
public class SnorkelZombie extends Zombie {

    public SnorkelZombie(int x, int y) {
        super("Snorkel Zombie", 100, true, 100, 1, x, y, "snorkel");
    }

    @Override
    public boolean isTargetable() {
        return isEating();
    }

    @Override
    public void draw(Graphics2D g) {
        drawSprite(g, isTargetable() ? 1f : 0.4f);
    }
}
