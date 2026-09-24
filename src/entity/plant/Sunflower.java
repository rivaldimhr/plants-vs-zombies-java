package entity.plant;

// Menghasilkan sun 25 secara berkala (sun harus diklik untuk diambil)
public class Sunflower extends SunProducerPlant {
    public static final double FIRST_SUN = 5; // detik
    public static final double SUN_INTERVAL = 12; // detik
    public static final int SUN_AMOUNT = 25;

    public Sunflower(int x, int y) {
        super("Sunflower", 100, 50, 10, FIRST_SUN, SUN_INTERVAL, x, y, "sunflower");
    }

    @Override
    protected int sunValue() {
        return SUN_AMOUNT;
    }
}
