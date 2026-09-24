package entity.zombie;

import java.awt.Color;

// Koran (100 HP) melindungi badan. Setelah koran hancur zombie marah: jalan & makan 2x lebih cepat.
public class NewspaperZombie extends Zombie {
    public static final int PAPER_HEALTH = 100;
    private static final Color ANGRY_TINT = new Color(255, 40, 30, 70);

    private boolean enraged = false;

    public NewspaperZombie(int x, int y) {
        super("Newspaper Zombie", 100, false, 100, 1, x, y, "newspaper");
        setArmor(new Armor(Armor.Kind.PAPER, PAPER_HEALTH, "newspaper"));
    }

    public boolean isEnraged() {
        return enraged;
    }

    @Override
    protected void onArmorLost(Armor lost) {
        enraged = true;
        walkDelay = WALK_DELAY / 2;
        attackSpeed = attackSpeed / 2;
    }

    @Override
    protected Color overlayTint() {
        Color slow = super.overlayTint();
        return slow != null ? slow : enraged ? ANGRY_TINT : null;
    }

}
