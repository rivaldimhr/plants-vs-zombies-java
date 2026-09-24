package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;

import game.Assets;
import game.Board;

// Kelas dasar untuk Plant dan Zombie
public abstract class Entity implements Updatable {
    protected final String name;
    protected int health;
    protected final int maxHealth;
    protected final boolean aquatic;
    protected int attackDamage;
    protected double attackSpeed; // detik per serangan
    protected int x, y; // posisi pixel (pojok kiri atas tile)
    protected String img;
    protected int timer = 0; // dalam tick

    public Entity(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int x, int y,
            String img) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.aquatic = aquatic;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.x = x;
        this.y = y;
        this.img = img;
    }

    public void takeDamage(int amount) {
        health -= amount;
    }

    public boolean isDead() {
        return health <= 0;
    }

    @Override
    public void update(Board board) {
    }

    // Gambar yang dipakai sekarang; bisa di-override (misalnya Wall-nut yang retak)
    protected String getImagePath() {
        return img;
    }

    public void draw(Graphics2D g) {
        drawSprite(g, 1f);
    }

    // Versi transparan (bayangan saat memilih tempat tanam)
    public void drawPreview(Graphics2D g, float alpha) {
        drawSprite(g, alpha);
    }

    // Gambar sprite di dalam tile: rasio dijaga, rata tengah-bawah
    protected void drawSprite(Graphics2D g, float alpha) {
        Image image = Assets.get(getImagePath());
        if (image == null) {
            return;
        }
        int w = image.getWidth(null);
        int h = image.getHeight(null);
        if (w <= 0 || h <= 0) {
            return;
        }
        double scale = Math.min((double) Board.TILE_SIZE / w, (double) Board.TILE_SIZE / h);
        int dw = (int) Math.round(w * scale);
        int dh = (int) Math.round(h * scale);
        int dx = x + (Board.TILE_SIZE - dw) / 2;
        int dy = y + Board.TILE_SIZE - dh;

        Composite old = g.getComposite();
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        g.drawImage(image, dx, dy, dw, dh, Assets.observer());
        g.setComposite(old);
    }

    // Bar HP kecil di atas sprite, hanya muncul kalau sudah terkena damage
    public void drawHealthBar(Graphics2D g) {
        if (health >= maxHealth || health <= 0) {
            return;
        }
        int barWidth = 40;
        int bx = x + (Board.TILE_SIZE - barWidth) / 2;
        int by = y + 2;
        double ratio = (double) health / maxHealth;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(bx - 1, by - 1, barWidth + 2, 6);
        g.setColor(ratio > 0.5 ? new Color(80, 200, 60) : ratio > 0.25 ? new Color(240, 170, 30) : new Color(220, 50, 40));
        g.fillRect(bx, by, (int) Math.ceil(barWidth * ratio), 4);
    }

    // getter and setter
    public String getName() {
        return name;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isAquatic() {
        return aquatic;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCol() {
        return x / Board.TILE_SIZE;
    }

    public int getRow() {
        return y / Board.TILE_SIZE;
    }

    public void setHealth(int health) {
        this.health = health;
    }

}
