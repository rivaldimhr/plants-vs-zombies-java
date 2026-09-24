package entity.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

import game.Board;

// Api Jalapeno yang membakar satu baris penuh
public class FireRowEffect extends Effect {
    private final int row;

    public FireRowEffect(int row) {
        super(seconds(1.1));
        this.row = row;
    }

    @Override
    public void draw(Graphics2D g) {
        double p = progress();
        int bottom = (row + 1) * Board.TILE_SIZE - 4;
        double height = Board.TILE_SIZE * 1.1 * Math.sin(Math.PI * Math.min(1, p * 1.2));
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.35)));
        for (int i = 0; i < 26; i++) {
            double fx = 50 + i * (Board.WIDTH - 60) / 26.0;
            double flicker = 0.7 + 0.3 * Math.sin(age * 0.6 + i * 1.7);
            double h = height * flicker;
            double w = 26;
            Path2D flame = new Path2D.Double();
            flame.moveTo(fx - w / 2, bottom);
            flame.quadTo(fx - w / 2, bottom - h * 0.6, fx + Math.sin(age * 0.3 + i) * 6, bottom - h);
            flame.quadTo(fx + w / 2, bottom - h * 0.6, fx + w / 2, bottom);
            flame.closePath();
            g2.setPaint(new GradientPaint((float) fx, bottom, new Color(255, 80, 10), (float) fx, (float) (bottom - h),
                    new Color(255, 230, 90)));
            g2.fill(flame);
        }
        g2.dispose();
    }
}
