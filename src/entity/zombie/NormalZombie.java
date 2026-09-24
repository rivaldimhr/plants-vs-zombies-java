package entity.zombie;

import java.awt.Rectangle;

import entity.effect.Effect;
import game.Sprite;

// Zombie biasa, hanya di darat
public class NormalZombie extends Zombie {

    public NormalZombie(int x, int y) {
        super("Normal Zombie", 125, false, 100, 1, x, y, "zombie");
    }

    @Override
    protected Effect normalDeathEffect(Sprite sprite, int frame, Rectangle bounds) {
        return dyingAnimation("zombie_dying");
    }

}
