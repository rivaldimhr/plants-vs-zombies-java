package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

// Pesan singkat di bawah layar yang hilang sendiri (pengganti dialog popup)
public class Toast {
    private static final long DURATION_MS = 1800;

    private volatile String message;
    private volatile long shownAt;

    public void show(String message) {
        this.message = message;
        this.shownAt = System.currentTimeMillis();
    }

    public void clear() {
        message = null;
    }

    public String getMessage() {
        return isVisible() ? message : null;
    }

    public boolean isVisible() {
        return message != null && System.currentTimeMillis() - shownAt < DURATION_MS;
    }

    public void draw(Graphics2D g, int centerY) {
        String text = getMessage();
        if (text == null) {
            return;
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(text) + 28;
        int h = fm.getHeight() + 12;
        int x = (Board.WIDTH - w) / 2;
        int y = centerY - h / 2;
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, w, h, 14, 14);
        g.setColor(Color.WHITE);
        g.drawString(text, x + 14, y + 6 + fm.getAscent());
    }
}
