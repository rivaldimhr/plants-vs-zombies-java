package entity.plant;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import game.Board;
import game.Game;
import game.GameEvent;

// Tanaman yang menembak tiap attackSpeed detik selama ada zombie dalam range
public abstract class ShooterPlant extends Plant {
    private static final int RECOIL_TICKS = 10;
    private int recoil = 0;

    public ShooterPlant(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int cost,
            int range, int cooldown, int x, int y, String spriteId) {
        super(name, health, aquatic, attackDamage, attackSpeed, cost, range, cooldown, x, y, spriteId);
    }

    protected abstract void shoot(Board board);

    @Override
    protected void act(Board board) {
        if (recoil > 0) {
            recoil--;
        }
        if (timer < attackSpeed * Game.UPS) {
            timer++;
        } else if (zombieInRange(board)) {
            shoot(board);
            board.fire(GameEvent.SHOOT);
            recoil = RECOIL_TICKS;
            timer = 0;
        }
    }

    // Animasi menembak: badan memendek-melebar sebentar (squash & stretch)
    @Override
    protected void applyTransform(Graphics2D g, Rectangle r) {
        if (recoil <= 0) {
            return;
        }
        double k = Math.sin(Math.PI * recoil / RECOIL_TICKS) * 0.12;
        double cx = r.getCenterX();
        double bottom = r.getMaxY();
        g.translate(cx, bottom);
        g.scale(1 + k, 1 - k);
        g.translate(-cx, -bottom);
    }
}
