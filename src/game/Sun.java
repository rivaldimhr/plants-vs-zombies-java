package game;

// Bank sun milik pemain dalam satu permainan
public class Sun {
    public static final int INITIAL_SUN = 50;

    private int amount = INITIAL_SUN;

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
}
