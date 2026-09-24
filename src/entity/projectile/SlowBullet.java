package entity.projectile;

import java.awt.Color;

import entity.DamageType;
import entity.zombie.Zombie;

// Peluru es dari Snow Pea: damage + memperlambat zombie
public class SlowBullet extends Bullet {

    public SlowBullet(int x, int y, int damage) {
        super(x, y, damage, "snowpea_bullet");
    }

    @Override
    protected void onHit(Zombie zombie) {
        zombie.takeDamage(getDamage(), DamageType.NORMAL);
        zombie.applySlow();
    }

    @Override
    protected Color splatColor() {
        return new Color(150, 210, 255);
    }

}
