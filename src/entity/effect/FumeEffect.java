package entity.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

// Asap ungu Fume-shroom yang menyembur ke kanan
public class FumeEffect extends Effect {
    private final int x, y, length;

    public FumeEffect(int x, int y, int length) {
        super(seconds(0.5));
        this.x = x;
        this.y = y;
        this.length = length;
    }

    @Override
    public void draw(Graphics2D g) {
        double p = progress();
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.75 * (1 - p))));
        for (int i = 0; i < 9; i++) {
            double f = i / 8.0;
            if (f > p * 1.6) {
                break;
            }
            int r = (int) (10 + 14 * f + 6 * p);
            int cx = (int) (x + length * f);
            int cy = (int) (y + Math.sin(i * 1.3 + age * 0.4) * 4);
            g2.setColor(i % 2 == 0 ? new Color(170, 110, 200) : new Color(210, 170, 230));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }
        g2.dispose();
    }
}
