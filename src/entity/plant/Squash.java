package entity.plant;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import entity.DamageType;
import entity.effect.ParticleEffect;
import entity.zombie.Zombie;
import game.Board;
import game.GameEvent;

/**
 * Melompat ke zombie terdekat (tile sendiri atau 1 tile di depan/belakang),
 * menghantamnya sampai gepeng, lalu Squash ikut hilang.
 */
public class Squash extends Plant {
    public static final int JUMP_TICKS = 24;
    private static final int REACH = 2 * TILE; // jarak zombie yang memicu lompatan

    private boolean jumping = false;
    private int targetX;
    private int jumpTimer = 0;

    public Squash(int x, int y) {
        super("Squash", 100, false, 5000, 0, 50, 1, 20, x, y, "squash");
    }

    public boolean isJumping() {
        return jumping;
    }

    // Saat sedang melompat, Squash tidak bisa dimakan lagi
    @Override
    public void takeDamage(int amount, DamageType type) {
        if (!jumping) {
            super.takeDamage(amount, type);
        }
    }

    @Override
    protected void act(Board board) {
        if (!jumping) {
            Zombie nearest = null;
            for (Zombie zombie : board.getZombies()) {
                int distance = Math.abs(zombie.getX() - x);
                if (zombie.getY() == y && !zombie.isDead() && distance <= REACH
                        && (nearest == null || distance < Math.abs(nearest.getX() - x))) {
                    nearest = zombie;
                }
            }
            if (nearest != null) {
                jumping = true;
                targetX = nearest.getX();
            }
            return;
        }
        jumpTimer++;
        if (jumpTimer >= JUMP_TICKS) {
            for (Zombie zombie : board.getZombies()) {
                if (zombie.getY() == y && Math.abs(zombie.getX() - targetX) <= TILE) {
                    zombie.takeDamage(attackDamage, DamageType.CRUSH);
                }
            }
            board.addEffect(ParticleEffect.dirt(targetX + TILE / 2.0, y + TILE - 4));
            board.fire(GameEvent.MINE_EXPLOSION);
            setHealth(0);
        }
    }

    // Lompatan: naik lalu menghantam ke posisi zombie
    @Override
    protected void applyTransform(Graphics2D g, Rectangle r) {
        if (!jumping) {
            return;
        }
        double t = (double) jumpTimer / JUMP_TICKS;
        double dx = (targetX - x) * Math.min(1, t * 1.3);
        double dy = -45 * Math.sin(Math.PI * Math.min(1, t * 1.15));
        g.translate(dx, dy);
    }
}
