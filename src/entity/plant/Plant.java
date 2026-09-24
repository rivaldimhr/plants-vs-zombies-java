package entity.plant;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import entity.Entity;
import entity.zombie.Zombie;
import game.Board;

public abstract class Plant extends Entity {
    protected final int cost;
    protected final int cooldown; // detik
    protected final int range; // dalam tile, -1 = satu baris penuh, 0 = tidak menyerang
    private boolean asleep = false;

    public Plant(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int cost, int range,
            int cooldown, int x, int y, String img) {
        super(name, health, aquatic, attackDamage, attackSpeed, x, y, img);
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
            int distance = zombie.getX() - x;
            if (zombie.getY() == y && zombie.isTargetable() && distance >= 0
                    && (range == -1 || distance <= range * Board.TILE_SIZE)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(Graphics2D g) {
        drawSprite(g, asleep ? 0.55f : 1f);
        if (asleep) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("z z", x + 36, y + 16);
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
