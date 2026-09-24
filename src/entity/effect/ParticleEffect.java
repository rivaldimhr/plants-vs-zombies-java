package entity.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// Sekumpulan partikel sederhana (pecahan kacang, tanah, abu, percikan air)
public class ParticleEffect extends Effect {

    private static final class Particle {
        double x, y, vx, vy;
        final double gravity, size;
        final Color color;

        Particle(double x, double y, double vx, double vy, double gravity, double size, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.gravity = gravity;
            this.size = size;
            this.color = color;
        }
    }

    private final List<Particle> particles = new ArrayList<>();

    private ParticleEffect(int lifetime) {
        super(lifetime);
    }

    private static double rnd(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private static ParticleEffect burst(double x, double y, int count, double speed, double gravity, double minSize,
            double maxSize, int lifetime, Color... colors) {
        ParticleEffect effect = new ParticleEffect(lifetime);
        for (int i = 0; i < count; i++) {
            double angle = rnd(0, Math.PI * 2);
            double v = rnd(speed * 0.3, speed);
            effect.particles.add(new Particle(x, y, Math.cos(angle) * v, Math.sin(angle) * v - speed * 0.4, gravity,
                    rnd(minSize, maxSize), colors[i % colors.length]));
        }
        return effect;
    }

    // Kacang pecah saat mengenai zombie
    public static ParticleEffect splat(double x, double y, Color color) {
        return burst(x, y, 7, 2.2, 0.15, 2, 5, seconds(0.35), color, color.brighter());
    }

    public static ParticleEffect dirt(double x, double y) {
        return burst(x, y, 12, 3.0, 0.2, 3, 6, seconds(0.6), new Color(110, 75, 40), new Color(80, 55, 30),
                new Color(60, 120, 40));
    }

    public static ParticleEffect ash(double x, double y) {
        return burst(x, y, 16, 1.6, -0.02, 2, 5, seconds(1.4), new Color(60, 55, 50), new Color(110, 100, 90),
                new Color(30, 28, 25));
    }

    public static ParticleEffect splash(double x, double y) {
        return burst(x, y, 14, 2.8, 0.18, 2, 5, seconds(0.8), new Color(170, 220, 255), new Color(230, 250, 255));
    }

    public static ParticleEffect sparks(double x, double y) {
        return burst(x, y, 22, 5.0, 0.12, 2, 5, seconds(0.7), new Color(255, 220, 80), new Color(255, 140, 30),
                new Color(255, 80, 20));
    }

    @Override
    public void update() {
        super.update();
        for (Particle p : particles) {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += p.gravity;
            p.vx *= 0.97;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.5)));
        for (Particle p : particles) {
            g2.setColor(p.color);
            int s = (int) Math.max(1, p.size);
            g2.fillOval((int) (p.x - s / 2.0), (int) (p.y - s / 2.0), s, s);
        }
        g2.dispose();
    }
}
