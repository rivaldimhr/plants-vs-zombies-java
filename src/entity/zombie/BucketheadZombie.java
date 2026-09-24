package entity.zombie;

import java.awt.Rectangle;

import entity.effect.Effect;
import game.Sprite;

// Memakai ember (175 HP). Setelah ember jatuh, jadi Normal Zombie.
public class BucketheadZombie extends Zombie {

    public BucketheadZombie(int x, int y) {
        super("Buckethead Zombie", 125, false, 100, 1, x, y, "zombie");
        setArmor(new Armor(Armor.Kind.BUCKET, 175, "buckethead"));
    }

    @Override
    protected Effect normalDeathEffect(Sprite sprite, int frame, Rectangle bounds) {
        return dyingAnimation("zombie_dying");
    }

}
