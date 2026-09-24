package entity.zombie;

import java.awt.Rectangle;

import entity.effect.Effect;
import game.Sprite;

// Berlari dengan perlengkapan rugby: helm (175 HP) dan 2x lebih cepat
public class FootballZombie extends Zombie {

    public FootballZombie(int x, int y) {
        super("Football Zombie", 125, false, 100, 1, x, y, "football");
        setArmor(new Armor(Armor.Kind.HELMET, 175, "football"));
        walkDelay = WALK_DELAY / 2;
    }

    @Override
    protected Effect normalDeathEffect(Sprite sprite, int frame, Rectangle bounds) {
        return dyingAnimation("football_dying");
    }

}
