package entity.plant;

import game.Board;
import game.Game;

public class Sunflower extends Plant {
    public static final int SUN_INTERVAL = 3; // detik
    public static final int SUN_AMOUNT = 25;

    public Sunflower(int x, int y) {
        super("Sunflower", 100, false, 0, 0, 50, 0, 10, x, y, "image/sprites/Sunflower.gif");
    }

    @Override
    protected void act(Board board) {
        if (timer >= SUN_INTERVAL * Game.UPS) {
            board.getSun().add(SUN_AMOUNT);
            timer = 0;
        } else {
            timer++;
        }
    }
}
