package entity.plant;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import game.Board;
import game.Game;

/**
 * Tanaman sekali pakai yang "menyala" beberapa saat setelah ditanam (membesar dan
 * memerah), lalu meledak: detonate() lalu mati. Contoh: Cherry Bomb, Jalapeno.
 */
public abstract class BombPlant extends Plant {
    private final int fuse; // tick

    public BombPlant(String name, int attackDamage, int cost, int range, int cooldown, double fuseSeconds, int x,
            int y, String spriteId) {
        super(name, 100, false, attackDamage, 0, cost, range, cooldown, x, y, spriteId);
        this.fuse = (int) (fuseSeconds * Game.UPS);
    }

    protected abstract void detonate(Board board);

    // Seperti di PvZ, tanaman peledak tidak bisa dimakan: pasti meledak setelah sumbunya habis
    @Override
    public void takeDamage(int amount, entity.DamageType type) {
    }

    @Override
    protected void act(Board board) {
        timer++;
        if (timer >= fuse) {
            detonate(board);
            setHealth(0);
        }
    }

    protected double fuseProgress() {
        return Math.min(1.0, (double) timer / fuse);
    }

    @Override
    protected void applyTransform(Graphics2D g, Rectangle r) {
        double scale = 1 + 0.35 * fuseProgress();
        double cx = r.getCenterX();
        double bottom = r.getMaxY();
        g.translate(cx, bottom);
        g.scale(scale, scale);
        g.translate(-cx, -bottom);
    }

    @Override
    protected Color overlayTint() {
        // makin lama makin merah dan berkedip
        int alpha = (int) (fuseProgress() * 150 * (0.6 + 0.4 * Math.sin(age * 0.8)));
        return alpha > 10 ? new Color(255, 40, 0, (alpha / 10) * 10) : null;
    }
}
