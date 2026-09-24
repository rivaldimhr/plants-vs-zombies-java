package entity.zombie;

import java.awt.Rectangle;

import entity.effect.Effect;
import game.Sprite;

// Memakai traffic cone (125 HP). Setelah cone jatuh, jadi Normal Zombie.
public class ConeheadZombie extends Zombie {

    public ConeheadZombie(int x, int y) {
        super("Conehead Zombie", 125, false, 100, 1, x, y, "zombie");
        setArmor(new Armor(Armor.Kind.CONE, 125, "conehead"));
    }

    @Override
    protected Effect normalDeathEffect(Sprite sprite, int frame, Rectangle bounds) {
        return dyingAnimation("zombie_dying");
    }

}
