package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Tombol yang bisa diklik. Di layar menu, gambar tombol sudah ada di background,
 * jadi tombol cukup menggambar highlight tipis saat disorot (drawHighlight).
 * Di dalam permainan (pause) tombol digambar lengkap (draw).
 */
public class MyButton {

	private String text;
	private final Rectangle bounds;
	private final Runnable onClick;
	private boolean mouseOver, mousePressed;
	private boolean enabled = true;
	private boolean visible = false; // true = tombol digambar lengkap, false = hanya highlight

	public MyButton(String text, int x, int y, int width, int height, Runnable onClick) {
		this.text = text;
		this.bounds = new Rectangle(x, y, width, height);
		this.onClick = onClick;
	}

	public void click() {
		if (enabled && onClick != null) {
			onClick.run();
		}
	}

	// Tombol lengkap: badan, border, teks
	public void draw(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (!enabled) {
			g.setColor(new Color(95, 95, 95));
		} else {
			g.setColor(mousePressed ? new Color(70, 120, 40) : mouseOver ? new Color(110, 175, 60) : new Color(90, 150, 50));
		}
		g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
		g.setColor(new Color(40, 70, 20));
		g.setStroke(new BasicStroke(2));
		g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 12, 12);
		g.setStroke(new BasicStroke(1));

		g.setColor(enabled ? Color.WHITE : new Color(190, 190, 190));
		g.setFont(new Font("Arial", Font.BOLD, bounds.height < 26 ? 12 : 14));
		FontMetrics fm = g.getFontMetrics();
		int tx = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
		int ty = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
		g.drawString(text, tx, ty);
	}

	// Highlight transparan di atas tombol yang sudah tergambar di background
	public void drawHighlight(Graphics2D g) {
		if (!mouseOver || !enabled) {
			return;
		}
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(new Color(255, 255, 255, mousePressed ? 110 : 70));
		g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 16, 16);
	}

	public void resetBooleans() {
		this.mouseOver = false;
		this.mousePressed = false;
	}

	public void setMousePressed(boolean mousePressed) {
		this.mousePressed = mousePressed;
	}

	public void setMouseOver(boolean mouseOver) {
		this.mouseOver = mouseOver;
	}

	public boolean contains(int x, int y) {
		return bounds.contains(x, y);
	}

	public Rectangle getBounds() {
		return bounds;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}
