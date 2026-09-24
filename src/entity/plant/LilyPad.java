package entity.plant;

// Ditanam di air, supaya tanaman darat bisa ditanam di atasnya
public class LilyPad extends Plant {

    public LilyPad(int x, int y) {
        super("Lily Pad", 100, true, 0, 0, 25, 0, 10, x, y, "image/sprites/lily_pad.gif");
    }
}
