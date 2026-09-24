package entity.effect;

import java.awt.Graphics2D;

import game.Game;

/**
 * Efek visual sementara (animasi mati, ledakan, partikel). Tidak memengaruhi
 * permainan; hanya digambar lalu hilang setelah lifetime habis.
 */
public abstract class Effect {
    protected final int lifetime; // tick
    protected int age = 0;

    protected Effect(int lifetime) {
        this.lifetime = lifetime;
    }

    protected static int seconds(double s) {
        return (int) Math.round(s * Game.UPS);
    }

    public void update() {
        age++;
    }

    public boolean isDone() {
        return age >= lifetime;
    }

    // 0.0 di awal, 1.0 di akhir
    protected double progress() {
        return Math.min(1.0, (double) age / lifetime);
    }

    // Alpha yang memudar di bagian akhir (fadePortion = bagian akhir yang memudar)
    protected float fadeAlpha(double fadePortion) {
        double p = progress();
        double start = 1.0 - fadePortion;
        return p <= start ? 1f : (float) Math.max(0, 1 - (p - start) / fadePortion);
    }

    public abstract void draw(Graphics2D g);
}
