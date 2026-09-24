package entity.projectile;

import entity.plant.PuffShroom;
import game.Board;

// Spora Puff-shroom: jarak tempuhnya pendek
public class PuffBullet extends Bullet {

    public PuffBullet(int x, int y, int damage) {
        super(x, y, damage, "image/sprites/PuffShroom_puff1.png");
    }

    @Override
    protected int maxDistance() {
        return (PuffShroom.RANGE + 1) * Board.TILE_SIZE;
    }

}
