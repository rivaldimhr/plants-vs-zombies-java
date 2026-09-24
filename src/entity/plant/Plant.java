package entity.plant;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import entity.Entity;
import entity.zombie.Zombie;
import game.Board;

public abstract class Plant extends Entity {
    private static final Color SLEEP_TINT = new Color(20, 20, 70, 110);

    protected final int cost;
    protected final int cooldown; // detik
    protected final int range; // dalam tile, -1 = satu baris penuh, 0 = tidak menyerang
    private boolean asleep = false;

    public Plant(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int cost, int range,
            int cooldown, int x, int y, String spriteId) {
        super(name, health, aquatic, attackDamage, attackSpeed, x, y, spriteId);
        this.cost = cost;
        this.cooldown = cooldown;
        this.range = range;
    }

    // Tanaman malam (jamur) tidur saat siang hari
    protected boolean isNocturnal() {
        return false;
    }

    public boolean isAsleep() {
        return asleep;
    }

    // Template method: subclass mengisi act(), pengecekan tidur diurus di sini
    @Override
    public final void update(Board board) {
        asleep = isNocturnal() && board.isDay();
        if (!asleep) {
            act(board);
        }
    }

    protected void act(Board board) {
    }

    // true kalau ada zombie yang bisa diserang di baris yang sama, di depan tanaman, dalam range
    protected boolean zombieInRange(Board board) {
        if (range == 0) {
            return false;
        }
        for (Zombie zombie : board.getZombies()) {
            if (inRange(zombie)) {
                return true;
            }
        }
        return false;
    }

    protected boolean inRange(Zombie zombie) {
        int distance = zombie.getX() - x;
        return zombie.getY() == y && zombie.isTargetable() && !zombie.isDead() && distance >= 0
                && (range == -1 || distance <= range * TILE);
    }

    @Override
    protected double animationSpeed() {
        return asleep ? 0 : 1;
    }

    @Override
    protected Color overlayTint() {
        return asleep ? SLEEP_TINT : null;
    }

    @Override
    public void draw(Graphics2D g) {
        super.draw(g);
        if (asleep) {
            // huruf z melayang naik
            g.setFont(new Font("Arial", Font.BOLD, 11));
            for (int i = 0; i < 2; i++) {
                int phase = (age / 2 + i * 30) % 60;
                g.setColor(new Color(255, 255, 255, 230 - phase * 3));
                g.drawString("z", x + 38 + phase / 6, y + 22 - phase / 3);
            }
        }
    }

    // getter
    public int getCost() {
        return cost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getRange() {
        return range;
    }

}
