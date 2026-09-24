package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Kelas dasar Almanac (daftar tanaman / zombie): grid di kiri, detail di kanan.
 * Detail dibuat dari data di kode (bukan gambar statis), jadi selalu sesuai
 * dengan stat yang dipakai game.
 */
public abstract class AlmanacScreen extends BaseScreen {
	protected static final Rectangle GRID_PANEL = new Rectangle(14, 52, 330, 356);
	private static final Rectangle DETAIL_PANEL = new Rectangle(352, 52, 294, 356);

	private int selected = 0;

	protected AlmanacScreen(Game game) {
		super(game);
		addVisibleButton("Menu", 566, 12, 80, 28, () -> game.setStates(States.MENU));
	}

	// Dipanggil subclass di konstruktor setelah data siap
	protected void registerEntries() {
		for (int i = 0; i < entryCount(); i++) {
			int index = i;
			Rectangle r = entryBounds(i);
			addButton(entryName(i), r.x, r.y, r.width, r.height, () -> selected = index);
		}
	}

	protected abstract String title();

	protected abstract int entryCount();

	protected abstract Rectangle entryBounds(int index);

	protected abstract void drawEntry(Graphics2D g, int index, Rectangle bounds);

	protected abstract String entryName(int index);

	protected abstract String entryDescription(int index);

	protected abstract List<String> entryStats(int index);

	protected abstract Sprite entrySprite(int index);

	@Override
	protected String getBackground() {
		return "image/IMAGE/BACKGROUND DAY.png";
	}

	@Override
	protected void renderContent(Graphics2D g) {
		UI.dim(g, 120);
		UI.outlinedTitle(g, title(), 180, 38, 26, new Color(255, 230, 120), UI.WOOD_DARK);

		UI.woodPanel(g, GRID_PANEL.x, GRID_PANEL.y, GRID_PANEL.width, GRID_PANEL.height);
		for (int i = 0; i < entryCount(); i++) {
			Rectangle r = entryBounds(i);
			drawEntry(g, i, r);
			if (i == selected) {
				g.setColor(new Color(255, 230, 0));
				g.setStroke(new BasicStroke(3));
				g.drawRoundRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4, 8, 8);
				g.setStroke(new BasicStroke(1));
			}
		}
		drawDetail(g);
	}

	private void drawDetail(Graphics2D g) {
		Rectangle p = DETAIL_PANEL;
		UI.woodPanel(g, p.x, p.y, p.width, p.height);
		// kotak preview animasi
		Rectangle box = new Rectangle(p.x + 14, p.y + 14, p.width - 28, 118);
		g.setColor(new Color(70, 130, 50));
		g.fillRoundRect(box.x, box.y, box.width, box.height, 10, 10);
		g.setColor(UI.WOOD_DARK);
		g.drawRoundRect(box.x, box.y, box.width, box.height, 10, 10);
		Sprite sprite = entrySprite(selected);
		if (!sprite.isEmpty()) {
			BufferedImage frame = sprite.frameAt(System.currentTimeMillis(), true);
			double scale = Math.min(100.0 / frame.getWidth(), 104.0 / frame.getHeight());
			int w = (int) (frame.getWidth() * scale);
			int h = (int) (frame.getHeight() * scale);
			g.drawImage(frame, box.x + (box.width - w) / 2, box.y + box.height - h - 6, w, h, null);
		}

		UI.centered(g, entryName(selected), p.x + p.width / 2, box.y + box.height + 24, UI.font(17),
				new Color(255, 230, 120));
		UI.paperPanel(g, p.x + 12, box.y + box.height + 32, p.width - 24, p.height - box.height - 58);
		Font font = UI.plain(12);
		int y = box.y + box.height + 50;
		g.setColor(UI.WOOD_DARK);
		for (String line : UI.wrap(g, entryDescription(selected), font, p.width - 44)) {
			g.setFont(font);
			g.drawString(line, p.x + 22, y);
			y += 15;
		}
		y += 6;
		for (String stat : entryStats(selected)) {
			g.setFont(UI.font(12));
			g.setColor(new Color(110, 60, 20));
			g.drawString(stat, p.x + 22, y);
			y += 16;
		}
	}

	public int getSelected() {
		return selected;
	}
}
