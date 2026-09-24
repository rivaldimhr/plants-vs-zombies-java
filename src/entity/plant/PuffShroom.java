package entity.plant;

import entity.projectile.PuffBullet;
import game.Board;

// Jamur: tidur saat siang, menembak spora jarak pendek (3 tile) saat malam
public class PuffShroom extends ShooterPlant {
    public static final int RANGE = 3;

    public PuffShroom(int x, int y) {
        super("Puff-shroom", 100, false, 15, 4, 0, RANGE, 7, x, y, "image/sprites/PuffShroom.png");
    }

    @Override
    protected boolean isNocturnal() {
        return true;
    }

    @Override
    protected void shoot(Board board) {
        board.addBullet(new PuffBullet(x, y, attackDamage));
    }
}
