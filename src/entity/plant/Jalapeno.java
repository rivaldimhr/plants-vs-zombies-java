package entity.plant;

import entity.DamageType;
import entity.effect.FireRowEffect;
import entity.zombie.Zombie;
import game.Board;
import game.GameEvent;

// Meledak setelah 1 detik: membakar semua zombie di satu baris
public class Jalapeno extends BombPlant {
    public static final double FUSE = 1.0;

    public Jalapeno(int x, int y) {
        super("Jalapeno", 1800, 125, -1, 50, FUSE, x, y, "jalapeno");
    }

    @Override
    protected void detonate(Board board) {
        for (Zombie zombie : board.getZombies()) {
            if (zombie.getY() == y) {
                zombie.takeDamage(attackDamage, DamageType.FIRE);
            }
        }
        board.addEffect(new FireRowEffect(getRow()));
        board.fire(GameEvent.FIRE);
    }
}
