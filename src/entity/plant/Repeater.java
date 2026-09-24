package entity.plant;

import entity.projectile.PeaBullet;
import game.Board;

public class Repeater extends ShooterPlant {

    public Repeater(int x, int y) {
        super("Repeater", 100, false, 25, 2, 200, -1, 10, x, y, "image/sprites/repeater.gif");
    }

    // Menembak 2 kacang sekaligus (kacang kedua sedikit di belakang)
    @Override
    protected void shoot(Board board) {
        board.addBullet(new PeaBullet(x, y, attackDamage));
        board.addBullet(new PeaBullet(x - 25, y, attackDamage));
    }
}
