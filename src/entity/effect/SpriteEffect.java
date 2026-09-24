package entity.effect;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import game.Game;
import game.Sprite;

/**
 * Memutar sprite satu kali dari frame pertama (misalnya animasi zombie mati),
 * lalu frame terakhir diam sebentar dan memudar.
 */
public class SpriteEffect extends Effect {
    private static final double HOLD_SECONDS = 0.8;

    private final Sprite sprite;
    private final Rectangle bounds;

    public SpriteEffect(Sprite sprite, Rectangle bounds) {
        super((int) (sprite.getDurationMs() * Game.UPS / 1000) + seconds(HOLD_SECONDS));
        this.sprite = sprite;
        this.bounds = bounds;
    }

    // Letakkan sprite dengan tinggi tertentu, rata tengah-bawah di titik (centerX, bottomY)
    public static SpriteEffect at(Sprite sprite, int centerX, int bottomY, int height) {
        int w = sprite.getWidth() * height / sprite.getHeight();
        return new SpriteEffect(sprite, new Rectangle(centerX - w / 2, bottomY - height, w, height));
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage frame = sprite.frameAt(age * 1000L / Game.UPS, false);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(0.3)));
        g2.drawImage(frame, bounds.x, bounds.y, bounds.width, bounds.height, null);
        g2.dispose();
    }
}
