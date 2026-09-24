package entity.zombie;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import entity.DamageType;
import entity.Entity;
import entity.effect.ArmorDropEffect;
import entity.effect.DeathEffect;
import entity.effect.Effect;
import entity.effect.SpriteEffect;
import entity.plant.LilyPad;
import entity.plant.Plant;
import game.Assets;
import game.Board;
import game.Game;
import game.GameEvent;
import game.Sprite;

public abstract class Zombie extends Entity {
    public static final int WALK_DELAY = 10; // jalan 1 pixel tiap (WALK_DELAY + 1) tick ≈ 5,5 px/detik
    public static final int SLOW_DURATION = 3 * Game.UPS;
    private static final int CONTACT_RANGE = 90; // jarak (px) zombie mulai memakan tanaman
    private static final Color SLOW_TINT = new Color(80, 150, 255, 110);

    protected int walkDelay = WALK_DELAY;
    protected Plant target;
    protected Armor armor;
    private Armor droppedArmor; // armor yang baru lepas, diambil Board untuk efek jatuh
    private boolean slowed = false;
    private int slowTime = 0;

    public Zombie(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int x, int y,
            String spriteId) {
        super(name, health, aquatic, attackDamage, attackSpeed, x, y, spriteId);
    }

    protected void setArmor(Armor armor) {
        this.armor = armor;
    }

    public Armor getArmor() {
        return armor;
    }

    // HP badan + armor
    public int getTotalHealth() {
        return health + (armor == null ? 0 : armor.getHealth());
    }

    @Override
    public void takeDamage(int amount, DamageType type) {
        int rest = amount;
        if (armor != null) {
            rest = armor.absorb(amount);
            if (armor.isBroken()) {
                Armor lost = armor;
                armor = null;
                droppedArmor = lost;
                onArmorLost(lost);
            }
        }
        super.takeDamage(rest, type);
    }

    protected void onArmorLost(Armor lost) {
    }

    // Diambil Board untuk membuat efek armor jatuh
    public Armor pollDroppedArmor() {
        Armor result = droppedArmor;
        droppedArmor = null;
        return result;
    }

    // Mati seketika (lawn mower)
    public void kill(DamageType type) {
        armor = null;
        health = 0;
        lastDamage = type;
    }

    // Dipanggil SlowBullet: kecepatan jalan & serang jadi setengah selama 3 detik
    public void applySlow() {
        slowed = true;
        slowTime = 0;
    }

    public boolean isSlowed() {
        return slowed;
    }

    // Bisa kena peluru atau tidak (Snorkel Zombie tidak bisa saat menyelam)
    public boolean isTargetable() {
        return true;
    }

    public boolean isEating() {
        return target != null;
    }

    protected void move() {
        x--;
    }

    // Tanaman di depan zombie (baris sama, dalam jarak kontak). Tanaman di atas Lily Pad didahulukan.
    protected Plant findTarget(Board board) {
        Plant found = null;
        for (Plant plant : board.getPlants()) {
            if (plant.getY() == y && !plant.isDead() && x >= plant.getX() && x - CONTACT_RANGE <= plant.getX()) {
                if (found == null || found instanceof LilyPad) {
                    found = plant;
                }
            }
        }
        return found;
    }

    @Override
    public void update(Board board) {
        if (slowed && ++slowTime >= SLOW_DURATION) {
            slowed = false;
        }
        int factor = slowed ? 2 : 1;

        target = findTarget(board);
        if (target == null) {
            if (timer >= walkDelay * factor) {
                move();
                timer = 0;
            } else {
                timer++;
            }
        } else {
            if (timer >= attackSpeed * Game.UPS * factor) {
                target.takeDamage(attackDamage, DamageType.NORMAL);
                board.fire(target.isDead() ? GameEvent.PLANT_EATEN : GameEvent.CHOMP);
                timer = 0;
            } else {
                timer++;
            }
        }
    }

    // ------------------------------------------------------------------ tampilan

    @Override
    protected String getSpriteId() {
        return armor != null ? armor.getSpriteId() : spriteId;
    }

    @Override
    protected double animationSpeed() {
        double speed = WALK_DELAY / (double) Math.max(1, walkDelay);
        if (isEating()) {
            speed = 0.6;
        }
        return slowed ? speed * 0.5 : speed;
    }

    @Override
    protected int drawMaxWidth() {
        return 84;
    }

    @Override
    protected int drawMaxHeight() {
        return 78; // zombie lebih tinggi dari satu tile, seperti di PvZ
    }

    @Override
    protected Color overlayTint() {
        return slowed ? SLOW_TINT : null;
    }

    // Animasi makan: badan mengangguk maju-mundur
    @Override
    protected void applyTransform(Graphics2D g, Rectangle r) {
        if (!isEating()) {
            return;
        }
        double angle = -0.12 * Math.abs(Math.sin(age * (slowed ? 0.1 : 0.2)));
        g.rotate(angle, r.getCenterX(), r.getMaxY());
    }

    @Override
    public void drawHealthBar(Graphics2D g) {
        int armorHealth = armor == null ? 0 : armor.getHealth();
        int armorMax = armor == null ? 0 : armor.getMaxHealth();
        drawBar(g, health, armorHealth, maxHealth + armorMax, -12);
    }

    public Effect createArmorDropEffect(Armor lost) {
        Rectangle r = currentBounds();
        return new ArmorDropEffect(lost.getKind(), r.getCenterX(), r.y + 8, y + TILE - 6);
    }

    // Efek saat zombie mati, tergantung cara matinya
    public Effect createDeathEffect() {
        Sprite sprite = sprite();
        int frame = sprite.frameIndex(animationTimeMs(), true);
        Rectangle bounds = drawBounds(sprite.frame(frame).getWidth(), sprite.frame(frame).getHeight());
        switch (lastDamage) {
            case EXPLOSION:
            case FIRE:
                return new DeathEffect(sprite, frame, bounds, DeathEffect.Style.BURNT);
            case DROWN:
                return new DeathEffect(sprite, frame, bounds, DeathEffect.Style.SINK);
            case CRUSH:
                return new DeathEffect(sprite, frame, bounds, DeathEffect.Style.FLAT);
            case MOWER:
                return new DeathEffect(sprite, frame, bounds, DeathEffect.Style.FLING);
            default:
                return normalDeathEffect(sprite, frame, bounds);
        }
    }

    // Mati biasa (kena peluru / asap); subclass bisa memakai animasi mati khusus
    protected Effect normalDeathEffect(Sprite sprite, int frame, Rectangle bounds) {
        return new DeathEffect(sprite, frame, bounds, DeathEffect.Style.FALL);
    }

    // Animasi mati dari sprite khusus (misalnya "zombie_dying")
    protected Effect dyingAnimation(String spriteId) {
        return SpriteEffect.at(Assets.sprite(spriteId), x + TILE / 2, y + TILE - 2, drawMaxHeight());
    }

    public int getWalkDelay() {
        return walkDelay;
    }

}
