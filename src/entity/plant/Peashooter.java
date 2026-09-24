package entity.plant;

import entity.projectile.PeaBullet;
import game.Board;

public class Peashooter extends ShooterPlant {

    public Peashooter(int x, int y) {
        super("Peashooter", 100, false, 25, 4, 100, -1, 10, x, y, "peashooter");
    }

    @Override
    protected void shoot(Board board) {
        board.addBullet(new PeaBullet(x, y, attackDamage));
    }
}
