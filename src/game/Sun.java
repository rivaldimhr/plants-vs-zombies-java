package game;

import java.util.random.RandomGenerator;

// Jumlah sun yang dimiliki pemain dalam satu permainan
public class Sun {
    public static final int INITIAL_SUN = 50;
    public static final int DROP_AMOUNT = 25;
    public static final int MIN_DROP_DELAY = 5; // detik
    public static final int MAX_DROP_DELAY = 10; // detik

    private final RandomGenerator random;
    private int amount = INITIAL_SUN;
    private int timer = 0;
    private int nextDrop;

    public Sun(RandomGenerator random) {
        this.random = random;
        this.nextDrop = nextDropDelay();
    }

    // Sun jatuh otomatis tiap 5-10 detik (dipanggil Board tiap tick saat siang)
    public synchronized void update() {
        timer++;
        if (timer >= nextDrop) {
            amount += DROP_AMOUNT;
            timer = 0;
            nextDrop = nextDropDelay();
        }
    }

    public synchronized void add(int value) {
        amount += value;
    }

    // Kurangi sun kalau cukup. Return false kalau sun tidak cukup.
    public synchronized boolean spend(int cost) {
        if (amount < cost) {
            return false;
        }
        amount -= cost;
        return true;
    }

    public synchronized int get() {
        return amount;
    }

    private int nextDropDelay() {
        return random.nextInt(MIN_DROP_DELAY * Game.UPS, MAX_DROP_DELAY * Game.UPS + 1);
    }
}
