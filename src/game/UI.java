package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

// Helper gambar UI yang dipakai banyak layar (panel kayu, teks berbayang, dll.)
public final class UI {
    public static final Color WOOD_DARK = new Color(70, 40, 15);
    public static final Color WOOD = new Color(120, 72, 30);
    public static final Color WOOD_LIGHT = new Color(160, 105, 50);
    public static final Color PAPER = new Color(250, 225, 170);

    private UI() {
    }

    public static Font font(int size) {
        return new Font("Arial", Font.BOLD, size);
    }

    public static Font plain(int size) {
        return new Font("Arial", Font.PLAIN, size);
    }

    public static void antialias(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // Panel kayu dengan bingkai (gaya menu PvZ)
    public static void woodPanel(Graphics2D g, int x, int y, int w, int h) {
        antialias(g);
        g.setPaint(new GradientPaint(x, y, WOOD_LIGHT, x, y + h, WOOD));
        g.fillRoundRect(x, y, w, h, 18, 18);
        g.setColor(WOOD_DARK);
        g.setStroke(new BasicStroke(4));
        g.drawRoundRect(x + 2, y + 2, w - 4, h - 4, 16, 16);
        g.setStroke(new BasicStroke(1));
    }

    // Panel kertas terang di dalam panel kayu
    public static void paperPanel(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(PAPER);
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(new Color(150, 110, 60));
        g.drawRoundRect(x, y, w, h, 12, 12);
    }

    // Teks rata tengah dengan bayangan
    public static void centered(Graphics2D g, String text, int centerX, int baselineY, Font font, Color color) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        shadowText(g, text, centerX - fm.stringWidth(text) / 2, baselineY, color);
    }

    public static void shadowText(Graphics2D g, String text, int x, int y, Color color) {
        antialias(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(text, x + 2, y + 2);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    // Teks judul besar dengan garis tepi tebal (seperti tulisan "FINAL WAVE")
    public static void outlinedTitle(Graphics2D g, String text, int centerX, int baselineY, int size, Color fill,
            Color outline) {
        antialias(g);
        g.setFont(font(size));
        FontMetrics fm = g.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        g.setColor(outline);
        int o = Math.max(2, size / 14);
        for (int dx = -o; dx <= o; dx++) {
            for (int dy = -o; dy <= o; dy++) {
                if (dx * dx + dy * dy <= o * o + 1) {
                    g.drawString(text, x + dx, baselineY + dy);
                }
            }
        }
        g.setColor(fill);
        g.drawString(text, x, baselineY);
    }

    // Pecah teks menjadi beberapa baris sesuai lebar
    public static List<String> wrap(Graphics2D g, String text, Font font, int width) {
        FontMetrics fm = g.getFontMetrics(font);
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) > width && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    // Latar gelap transparan di atas layar
    public static void dim(Graphics2D g, int alpha) {
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, Board.WIDTH, Board.HEIGHT);
    }
}
