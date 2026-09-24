package entity.projectile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import entity.DamageType;
import entity.Updatable;
import entity.effect.ParticleEffect;
import entity.zombie.Zombie;
import game.Assets;
import game.Board;
import game.GameEvent;

public abstract class Bullet implements Updatable {
    public static final int SPEED = 3; // pixel per tick = 180 px/detik
    private static final int SIZE = 20;

    private final int startX;
    private int x;
    private final int y;
    private final int damage;
    private final String spriteId;
    private boolean done = false; // true kalau sudah kena zombie / keluar layar / habis jarak

    public Bullet(int x, int y, int damage, String spriteId) {
        this.startX = x;
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.spriteId = spriteId;
    }

    // Jarak tempuh maksimal (pixel); default sampai keluar layar
    protected int maxDistance() {
        return Integer.MAX_VALUE;
    }

    // Efek saat mengenai zombie; subclass bisa menambah efek (misalnya slow)
    protected void onHit(Zombie zombie) {
        zombie.takeDamage(damage, DamageType.NORMAL);
    }

    // Warna pecahan saat mengenai zombie
    protected Color splatColor() {
        return new Color(120, 200, 60);
    }

    private boolean hits(Zombie zombie) {
        return y == zombie.getY() && x <= zombie.getX() && x + 90 >= zombie.getX();
    }

    public boolean isDone() {
        return done;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public void draw(Graphics2D g) {
        BufferedImage image = Assets.sprite(spriteId).frame(0);
        g.drawImage(image, x + 35, y + 12, SIZE, SIZE, null);
    }

    @Override
    public void update(Board board) {
        x += SPEED;
        for (Zombie zombie : board.getZombies()) {
            if (!zombie.isDead() && zombie.isTargetable() && hits(zombie)) {
                onHit(zombie);
                board.addEffect(ParticleEffect.splat(x + 45, y + 22, splatColor()));
                board.fire(GameEvent.HIT);
                done = true;
                return; // satu peluru hanya kena satu zombie
            }
        }
        if (x > Board.WIDTH || x - startX > maxDistance()) {
            done = true;
        }
    }

}
