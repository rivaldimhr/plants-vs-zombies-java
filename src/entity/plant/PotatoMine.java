package entity.plant;

import entity.DamageType;
import entity.effect.ExplosionEffect;
import entity.effect.ParticleEffect;
import entity.zombie.Zombie;
import game.Board;
import game.Game;
import game.GameEvent;

/**
 * Ranjau murah: butuh 14 detik untuk muncul dari tanah (belum aktif, bisa dimakan).
 * Setelah aktif, meledak saat zombie menginjaknya dan menghancurkan zombie di tile itu.
 */
public class PotatoMine extends Plant {
    public static final int ARM_TIME = 14 * Game.UPS;
    private static final int TRIGGER_RANGE = 40;

    public PotatoMine(int x, int y) {
        super("Potato Mine", 100, false, 1800, 0, 25, 0, 30, x, y, "potatomine_unarmed");
    }

    public boolean isArmed() {
        return timer >= ARM_TIME;
    }

    @Override
    protected void act(Board board) {
        if (!isArmed()) {
            timer++;
            if (isArmed()) {
                board.addEffect(ParticleEffect.dirt(x + TILE / 2.0, y + TILE - 6));
            }
            return;
        }
        boolean triggered = false;
        for (Zombie zombie : board.getZombies()) {
            if (zombie.getY() == y && !zombie.isDead() && zombie.getX() >= x - TRIGGER_RANGE / 2
                    && zombie.getX() <= x + TRIGGER_RANGE) {
                triggered = true;
                break;
            }
        }
        if (!triggered) {
            return;
        }
        for (Zombie zombie : board.getZombies()) {
            if (zombie.getY() == y && Math.abs(zombie.getX() - x) <= TILE) {
                zombie.takeDamage(attackDamage, DamageType.EXPLOSION);
            }
        }
        board.addEffect(new ExplosionEffect(x + TILE / 2, y + TILE - 12, TILE, "SPUDOW!"));
        board.addEffect(ParticleEffect.dirt(x + TILE / 2.0, y + TILE - 6));
        board.fire(GameEvent.MINE_EXPLOSION);
        setHealth(0);
    }

    @Override
    protected String getSpriteId() {
        return isArmed() ? "potatomine" : "potatomine_unarmed";
    }

    @Override
    protected int drawMaxHeight() {
        return isArmed() ? 46 : 30;
    }
}
