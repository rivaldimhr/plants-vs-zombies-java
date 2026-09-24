package entity.plant;

import entity.DamageType;
import entity.effect.FumeEffect;
import entity.zombie.Zombie;
import game.Board;

// Jamur malam: menyemburkan asap yang menembus SEMUA zombie dalam 4 tile di depannya
public class FumeShroom extends ShooterPlant {
    public static final int RANGE = 4;

    public FumeShroom(int x, int y) {
        super("Fume-shroom", 100, false, 20, 4, 75, RANGE, 8, x, y, "fumeshroom");
    }

    @Override
    protected boolean isNocturnal() {
        return true;
    }

    @Override
    protected void shoot(Board board) {
        for (Zombie zombie : board.getZombies()) {
            if (inRange(zombie)) {
                zombie.takeDamage(attackDamage, DamageType.NORMAL);
            }
        }
        board.addEffect(new FumeEffect(x + TILE - 10, y + TILE / 2, RANGE * TILE));
    }
}
