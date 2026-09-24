package entity.plant;

import entity.projectile.SlowBullet;
import game.Board;

public class SnowPea extends ShooterPlant {

    public SnowPea(int x, int y) {
        super("Snow Pea", 100, false, 25, 4, 175, -1, 10, x, y, "image/sprites/snowpea.png");
    }

    @Override
    protected void shoot(Board board) {
        board.addBullet(new SlowBullet(x, y, attackDamage));
    }
}
