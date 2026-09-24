package entity.plant;

import entity.DamageType;
import entity.effect.ParticleEffect;
import entity.zombie.Zombie;
import game.Board;

// Tanaman air sekali pakai: menarik zombie terdekat ke bawah air
public class TangleKelp extends Plant {
    private static final int REACH = 2 * TILE;

    public TangleKelp(int x, int y) {
        super("Tangle Kelp", 100, true, 2000, 0, 25, 1, 15, x, y, "tanglekelp");
    }

    @Override
    protected void act(Board board) {
        boolean grabbed = false;
        for (Zombie zombie : board.getZombies()) {
            if (zombie.getY() == y && !zombie.isDead() && Math.abs(zombie.getX() - x) <= REACH) {
                zombie.takeDamage(attackDamage, DamageType.DROWN);
                grabbed = true;
            }
        }
        if (grabbed) {
            board.addEffect(ParticleEffect.splash(x + TILE / 2.0, y + TILE - 8));
            setHealth(0);
        }
    }

    @Override
    protected int drawMaxHeight() {
        return 40;
    }
}
