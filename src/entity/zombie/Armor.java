package entity.zombie;

/**
 * Pelindung zombie (cone, ember, koran, helm). Menyerap damage lebih dulu sebelum
 * badan zombie. Selama armor masih ada, zombie memakai sprite versi berarmor.
 * Zombie + Armor = komposisi (bukan subclass untuk tiap kombinasi).
 */
public class Armor {
    public enum Kind {
        CONE, BUCKET, PAPER, HELMET
    }

    private final Kind kind;
    private final int maxHealth;
    private final String spriteId; // sprite zombie saat masih memakai armor ini
    private int health;

    public Armor(Kind kind, int health, String spriteId) {
        this.kind = kind;
        this.maxHealth = health;
        this.health = health;
        this.spriteId = spriteId;
    }

    // Serap damage; return sisa damage yang tembus ke badan
    public int absorb(int damage) {
        if (damage <= health) {
            health -= damage;
            return 0;
        }
        int rest = damage - health;
        health = 0;
        return rest;
    }

    public boolean isBroken() {
        return health <= 0;
    }

    public Kind getKind() {
        return kind;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public String getSpriteId() {
        return spriteId;
    }
}
