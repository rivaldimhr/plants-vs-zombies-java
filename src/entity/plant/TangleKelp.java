package entity.plant;

// Versi air dari Squash: menarik zombie ke bawah air
public class TangleKelp extends InstantKillPlant {

    public TangleKelp(int x, int y) {
        super("Tangle Kelp", 100, true, 2000, 25, 15, x, y, "image/sprites/TangleKelp.gif");
    }
}
