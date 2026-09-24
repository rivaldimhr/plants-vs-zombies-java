package entity.plant;

import entity.DamageType;
import entity.effect.ExplosionEffect;
import entity.zombie.Zombie;
import game.Board;
import game.GameEvent;

// Meledak setelah 1,2 detik: menghancurkan semua zombie di area 3x3 tile
public class CherryBomb extends BombPlant {
    public static final double FUSE = 1.2;

    public CherryBomb(int x, int y) {
        super("Cherry Bomb", 1800, 150, 1, 50, FUSE, x, y, "cherrybomb");
    }

    @Override
    protected void detonate(Board board) {
        for (Zombie zombie : board.getZombies()) {
            int zombieCol = (zombie.getX() + TILE / 2) / TILE;
            if (Math.abs(zombieCol - getCol()) <= 1 && Math.abs(zombie.getRow() - getRow()) <= 1) {
                zombie.takeDamage(attackDamage, DamageType.EXPLOSION);
            }
        }
        board.addEffect(new ExplosionEffect(x + TILE / 2, y + TILE / 2, (int) (TILE * 1.6), null));
        board.fire(GameEvent.EXPLOSION);
    }
}
