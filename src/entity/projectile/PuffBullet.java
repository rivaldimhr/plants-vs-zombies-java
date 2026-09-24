package entity.projectile;

import java.awt.Color;

import entity.plant.PuffShroom;
import game.Board;

// Spora Puff-shroom: jarak tempuhnya pendek
public class PuffBullet extends Bullet {

    public PuffBullet(int x, int y, int damage) {
        super(x, y, damage, "puff_bullet");
    }

    @Override
    protected int maxDistance() {
        return (PuffShroom.RANGE + 1) * Board.TILE_SIZE;
    }

    @Override
    protected Color splatColor() {
        return new Color(200, 150, 220);
    }

}
