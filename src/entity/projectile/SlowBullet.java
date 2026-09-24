package entity.projectile;

import entity.zombie.Zombie;

// Peluru es dari Snow Pea: damage + memperlambat zombie
public class SlowBullet extends Bullet {

    public SlowBullet(int x, int y, int damage) {
        super(x, y, damage, "image/sprites/ProjectileSnowPea.png");
    }

    @Override
    protected void onHit(Zombie zombie) {
        super.onHit(zombie);
        zombie.applySlow();
    }

}
