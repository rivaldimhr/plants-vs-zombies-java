package entity.plant;

import java.util.concurrent.ThreadLocalRandom;

import entity.SunToken;
import game.Board;
import game.Game;

// Tanaman penghasil sun: sun muncul di dekat tanaman dan harus diklik untuk diambil
public abstract class SunProducerPlant extends Plant {
    private final int firstDelay; // tick
    private final int interval; // tick
    private boolean producedFirst = false;

    public SunProducerPlant(String name, int health, int cost, int cooldown, double firstDelaySeconds,
            double intervalSeconds, int x, int y, String spriteId) {
        super(name, health, false, 0, 0, cost, 0, cooldown, x, y, spriteId);
        this.firstDelay = (int) (firstDelaySeconds * Game.UPS);
        this.interval = (int) (intervalSeconds * Game.UPS);
    }

    protected abstract int sunValue();

    @Override
    protected void act(Board board) {
        timer++;
        if (timer >= (producedFirst ? interval : firstDelay)) {
            timer = 0;
            producedFirst = true;
            double dx = ThreadLocalRandom.current().nextDouble(-1.2, 1.2);
            board.addSun(SunToken.fromPlant(x + TILE / 2.0, y + TILE / 2.0, sunValue(), dx));
        }
    }
}
