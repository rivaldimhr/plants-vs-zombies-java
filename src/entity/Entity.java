package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import game.Assets;
import game.Board;
import game.Game;
import game.Sprite;

// Kelas dasar untuk Plant dan Zombie
public abstract class Entity implements Updatable {
    public static final int FLASH_TICKS = 8; // lama kilat putih saat terkena serangan
    protected static final int TILE = Board.TILE_SIZE;

    protected final String name;
    protected int health;
    protected final int maxHealth;
    protected final boolean aquatic;
    protected int attackDamage;
    protected double attackSpeed; // detik per serangan
    protected int x, y; // posisi pixel (pojok kiri atas tile)
    protected String spriteId;
    protected int timer = 0; // tick
    protected int age = 0; // tick sejak dibuat
    protected int hitFlash = 0;
    protected DamageType lastDamage = DamageType.NORMAL;
    private double animMs = 0; // waktu animasi (ms), ikut pause & kecepatan

    public Entity(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int x, int y,
            String spriteId) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.aquatic = aquatic;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.x = x;
        this.y = y;
        this.spriteId = spriteId;
    }

    public final void takeDamage(int amount) {
        takeDamage(amount, DamageType.NORMAL);
    }

    public void takeDamage(int amount, DamageType type) {
        health -= amount;
        lastDamage = type;
        hitFlash = FLASH_TICKS;
    }

    public boolean isDead() {
        return health <= 0;
    }

    @Override
    public void update(Board board) {
    }

    // Dipanggil Board tiap tick setelah update: memajukan waktu animasi
    public final void tickAnimation() {
        age++;
        if (hitFlash > 0) {
            hitFlash--;
        }
        animMs += 1000.0 / Game.UPS * animationSpeed();
    }

    // ------------------------------------------------------------------ tampilan

    protected String getSpriteId() {
        return spriteId;
    }

    protected Sprite sprite() {
        return Assets.sprite(getSpriteId());
    }

    // Sprite yang sedang dipakai (untuk Almanac)
    public Sprite getSprite() {
        return sprite();
    }

    // Kecepatan animasi relatif (misalnya 0.5 saat diperlambat)
    protected double animationSpeed() {
        return 1.0;
    }

    protected long animationTimeMs() {
        return (long) animMs;
    }

    // Ukuran maksimal gambar di layar; gambar dirata tengah-bawah di dalam tile
    protected int drawMaxWidth() {
        return TILE;
    }

    protected int drawMaxHeight() {
        return TILE - 4;
    }

    public Rectangle drawBounds(int imageWidth, int imageHeight) {
        double scale = Math.min((double) drawMaxWidth() / imageWidth, (double) drawMaxHeight() / imageHeight);
        int w = (int) Math.round(imageWidth * scale);
        int h = (int) Math.round(imageHeight * scale);
        return new Rectangle(x + (TILE - w) / 2, y + TILE - 2 - h, w, h);
    }

    // Hook animasi: transformasi (goyang, membal, melompat) sebelum gambar digambar
    protected void applyTransform(Graphics2D g, Rectangle bounds) {
    }

    // Hook warna: tint di atas gambar (biru saat lambat, gelap saat tidur), atau null
    protected Color overlayTint() {
        return null;
    }

    public BufferedImage currentFrame() {
        return sprite().frameAt(animationTimeMs(), true);
    }

    public Rectangle currentBounds() {
        BufferedImage frame = currentFrame();
        return drawBounds(frame.getWidth(), frame.getHeight());
    }

    public void draw(Graphics2D g) {
        drawSprite(g, 1f);
    }

    // Versi transparan (bayangan saat memilih tempat tanam)
    public void drawPreview(Graphics2D g, float alpha) {
        drawSprite(g, alpha);
    }

    protected void drawSprite(Graphics2D g, float alpha) {
        Sprite sprite = sprite();
        if (sprite.isEmpty()) {
            return;
        }
        int index = sprite.frameIndex(animationTimeMs(), true);
        Rectangle r = drawBounds(sprite.getWidth(), sprite.getHeight());
        // pakai versi sprite yang sudah seukuran tampilan (cepat, tanpa scaling tiap frame)
        Sprite fitted = sprite.scaledTo(r.width, r.height);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        if (alpha < 1f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        }
        applyTransform(g2, r);
        g2.drawImage(fitted.frame(index), r.x, r.y, null);
        Color tint = overlayTint();
        if (tint != null) {
            g2.drawImage(fitted.tinted(index, tint), r.x, r.y, null);
        }
        if (hitFlash > 0) {
            int strength = 150 * hitFlash / FLASH_TICKS;
            g2.drawImage(fitted.tinted(index, new Color(255, 255, 255, 25 * (strength / 25))), r.x, r.y, null);
        }
        g2.dispose();
    }

    // Bar HP kecil di atas sprite, hanya muncul kalau sudah terkena damage
    public void drawHealthBar(Graphics2D g) {
        drawBar(g, health, 0, maxHealth, 0);
    }

    // armor = HP pelindung (abu-abu) di kanan HP badan
    protected void drawBar(Graphics2D g, int body, int armor, int max, int topOffset) {
        if (body + armor >= max || body <= 0) {
            return;
        }
        int barWidth = 40;
        int bx = x + (TILE - barWidth) / 2;
        int by = y + 2 + topOffset;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(bx - 1, by - 1, barWidth + 2, 6);
        double ratio = (double) body / max;
        g.setColor(ratio > 0.5 ? new Color(80, 200, 60) : ratio > 0.25 ? new Color(240, 170, 30) : new Color(220, 50, 40));
        int bodyWidth = (int) Math.ceil(barWidth * ratio);
        g.fillRect(bx, by, bodyWidth, 4);
        if (armor > 0) {
            g.setColor(new Color(200, 200, 210));
            g.fillRect(bx + bodyWidth, by, (int) Math.ceil(barWidth * (double) armor / max), 4);
        }
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
        return x / TILE;
    }

    public int getRow() {
        return y / TILE;
    }

    public int getAge() {
        return age;
    }

    // true selama efek kilat putih (baru terkena serangan)
    public boolean isFlashing() {
        return hitFlash > 0;
    }

    public DamageType getLastDamage() {
        return lastDamage;
    }

    public void setHealth(int health) {
        this.health = health;
    }

}
