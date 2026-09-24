package entity.zombie;

// Koran menahan 100 damage pertama. Setelah koran hancur zombie marah: jalan & makan 2x lebih cepat.
public class NewspaperZombie extends Zombie {
    public static final int PAPER_HEALTH = 100;
    private boolean enraged = false;

    public NewspaperZombie(int x, int y) {
        super("Newspaper Zombie", 200, false, 100, 1, x, y, "image/sprites/Transparent_newspaper_idle.gif");
    }

    public boolean isEnraged() {
        return enraged;
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);
        if (!enraged && health <= maxHealth - PAPER_HEALTH) {
            enraged = true;
            walkDelay = WALK_DELAY / 2;
            attackSpeed = attackSpeed / 2;
        }
    }
}
