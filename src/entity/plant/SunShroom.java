package entity.plant;

import game.Game;

/**
 * Penghasil sun versi malam (jamur, tidur saat siang). Awalnya kecil dan
 * menghasilkan sun kecil (15); setelah 60 detik aktif tumbuh besar (25).
 */
public class SunShroom extends SunProducerPlant {
    public static final int SMALL_SUN = 15;
    public static final int BIG_SUN = 25;
    public static final int GROW_TIME = 60 * Game.UPS;

    private int activeTicks = 0;

    public SunShroom(int x, int y) {
        super("Sun-shroom", 100, 25, 8, 6, 12, x, y, "sunshroom");
    }

    @Override
    protected boolean isNocturnal() {
        return true;
    }

    public boolean isGrown() {
        return activeTicks >= GROW_TIME;
    }

    @Override
    protected void act(game.Board board) {
        activeTicks++;
        super.act(board);
    }

    @Override
    protected int sunValue() {
        return isGrown() ? BIG_SUN : SMALL_SUN;
    }

    @Override
    protected int drawMaxHeight() {
        return isGrown() ? 46 : 30;
    }
}
