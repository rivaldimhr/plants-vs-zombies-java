package entity.effect;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import game.Sprite;

/**
 * Animasi mati generik dari "foto" terakhir zombie:
 * FALL  = roboh ke belakang lalu memudar
 * BURNT = jadi siluet gosong lalu runtuh menjadi abu
 * SINK  = tenggelam ke kolam
 * FLAT  = gepeng (dihantam Squash)
 * FLING = terlempar (lawn mower)
 */
public class DeathEffect extends Effect {
    public enum Style {
        FALL, BURNT, SINK, FLAT, FLING
    }

    private final BufferedImage image;
    private final BufferedImage burnt;
    private final Rectangle bounds;
    private final Style style;
    private final ParticleEffect particles;

    public DeathEffect(Sprite sprite, int frameIndex, Rectangle bounds, Style style) {
        super(seconds(style == Style.BURNT ? 1.4 : style == Style.FLING ? 0.8 : 1.1));
        this.image = sprite.frame(frameIndex);
        this.burnt = style == Style.BURNT ? sprite.tinted(frameIndex, new Color(25, 20, 18, 255)) : null;
        this.bounds = bounds;
        this.style = style;
        int cx = bounds.x + bounds.width / 2;
        int bottom = bounds.y + bounds.height;
        switch (style) {
            case BURNT:
                particles = ParticleEffect.ash(cx, bottom - 10);
                break;
            case SINK:
                particles = ParticleEffect.splash(cx, bottom - 6);
                break;
            case FLAT:
                particles = ParticleEffect.dirt(cx, bottom);
                break;
            default:
                particles = null;
        }
    }

    @Override
    public void update() {
        super.update();
        if (particles != null) {
            particles.update();
        }
    }

    @Override
    public void draw(Graphics2D g) {
        double p = progress();
        Graphics2D g2 = (Graphics2D) g.create();
        int cx = bounds.x + bounds.width / 2;
        int bottom = bounds.y + bounds.height;
        switch (style) {
            case FALL:
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.4)));
                g2.rotate(Math.toRadians(80 * Math.min(1, p * 1.8)), cx + bounds.width / 4.0, bottom);
                g2.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);
                break;
            case BURNT: {
                // diam gosong sebentar, lalu runtuh (mengecil ke bawah) dan memudar
                double collapse = Math.max(0, (p - 0.45) / 0.55);
                int h = (int) (bounds.height * (1 - 0.85 * collapse));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.35)));
                g2.drawImage(burnt, bounds.x, bottom - h, bounds.width, h, null);
                break;
            }
            case SINK: {
                int waterline = bottom - 8;
                int sink = (int) (bounds.height * Math.min(1, p * 1.3));
                g2.clipRect(bounds.x - 20, bounds.y - 40, bounds.width + 40, waterline - bounds.y + 40);
                g2.drawImage(image, bounds.x, bounds.y + sink, bounds.width, bounds.height, null);
                break;
            }
            case FLAT: {
                int h = Math.max(4, (int) (bounds.height * 0.18));
                int w = (int) (bounds.width * 1.3);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.5)));
                g2.drawImage(image, cx - w / 2, bottom - h, w, h, null);
                break;
            }
            case FLING: {
                double dx = 260 * p;
                double dy = -120 * p + 200 * p * p;
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.5)));
                g2.rotate(Math.toRadians(540 * p), cx + dx, bounds.y + bounds.height / 2.0 + dy);
                g2.drawImage(image, (int) (bounds.x + dx), (int) (bounds.y + dy), bounds.width, bounds.height, null);
                break;
            }
            default:
                break;
        }
        g2.dispose();
        if (particles != null) {
            particles.draw(g);
        }
    }
}
