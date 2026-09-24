package game;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelas dasar layar menu: background gambar + daftar tombol.
 * Hover, press, dan klik semua tombol diurus di sini, jadi subclass cukup
 * mendaftarkan tombol lewat addButton(...).
 */
public abstract class BaseScreen implements ScreenMethod {

	protected final Game game;
	private final List<MyButton> buttons = new ArrayList<>();

	protected BaseScreen(Game game) {
		this.game = game;
	}

	// Path gambar background layar ini
	protected abstract String getBackground();

	protected MyButton addButton(String text, int x, int y, int width, int height, Runnable onClick) {
		MyButton button = new MyButton(text, x, y, width, height, onClick);
		buttons.add(button);
		return button;
	}

	// Hook untuk menggambar isi tambahan di atas background
	protected void renderContent(Graphics2D g) {
	}

	@Override
	public void render(Graphics2D g) {
		g.drawImage(Assets.get(getBackground()), 0, 0, Board.WIDTH, Board.HEIGHT, null);
		renderContent(g);
		for (MyButton button : buttons) {
			button.drawHighlight(g);
		}
	}

	@Override
	public void mouseClicked(int x, int y) {
		for (MyButton button : buttons) {
			if (button.contains(x, y)) {
				button.click();
				return;
			}
		}
	}

	@Override
	public void mouseMoved(int x, int y) {
		for (MyButton button : buttons) {
			button.setMouseOver(button.contains(x, y));
		}
	}

	@Override
	public void mousePressed(int x, int y) {
		for (MyButton button : buttons) {
			button.setMousePressed(button.contains(x, y));
		}
	}

	@Override
	public void mouseReleased(int x, int y) {
		for (MyButton button : buttons) {
			button.setMousePressed(false);
		}
	}

	// Dipanggil saat pindah ke layar lain, supaya highlight tidak tertinggal
	public void resetButtons() {
		for (MyButton button : buttons) {
			button.resetBooleans();
		}
	}

}
