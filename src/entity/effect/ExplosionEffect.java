package entity.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;

// Ledakan: kilat terang yang membesar, bola api, asap, dan percikan
public class ExplosionEffect extends Effect {
    private final int cx, cy, radius;
    private final ParticleEffect sparks;
    private final String text;

    // text: tulisan komik (misalnya "SPUDOW!" untuk Potato Mine), boleh null
    public ExplosionEffect(int cx, int cy, int radius, String text) {
        super(seconds(0.9));
        this.cx = cx;
        this.cy = cy;
        this.radius = radius;
        this.text = text;
        this.sparks = ParticleEffect.sparks(cx, cy);
    }

    @Override
    public void update() {
        super.update();
        sparks.update();
    }

    @Override
    public void draw(Graphics2D g) {
        double p = progress();
        Graphics2D g2 = (Graphics2D) g.create();

        // bola api: membesar cepat lalu memudar
        double grow = Math.min(1, p * 4);
        int r = (int) (radius * (0.3 + 0.7 * grow));
        float alpha = (float) Math.max(0, 1 - p * 1.2);
        if (alpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2.setPaint(new RadialGradientPaint(cx, cy, r, new float[] { 0f, 0.35f, 0.75f, 1f },
                    new Color[] { new Color(255, 255, 220), new Color(255, 210, 60), new Color(240, 90, 20),
                            new Color(120, 30, 10, 0) }));
            g2.fillOval(cx - r, cy - r, r * 2, r * 2);
        }

        // asap yang naik
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.6) * 0.6f));
        g2.setColor(new Color(70, 60, 55));
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3 + p;
            int sr = (int) (radius * 0.35 * (0.5 + p));
            int sx = (int) (cx + Math.cos(a) * radius * 0.5 * p);
            int sy = (int) (cy + Math.sin(a) * radius * 0.3 * p - 40 * p);
            g2.fillOval(sx - sr, sy - sr, sr * 2, sr * 2);
        }
        g2.dispose();
        sparks.draw(g);

        if (text != null && p < 0.8) {
            Graphics2D gt = (Graphics2D) g.create();
            gt.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
            int w = gt.getFontMetrics().stringWidth(text);
            gt.setColor(new Color(90, 40, 10));
            gt.drawString(text, cx - w / 2 + 2, cy - radius / 2 + 2);
            gt.setColor(new Color(255, 230, 120));
            gt.drawString(text, cx - w / 2, cy - radius / 2);
            gt.dispose();
        }
    }
}
