package game;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Kelas dasar layar menu: background + daftar tombol.
 * Hover, press, dan klik semua tombol diurus di sini, jadi subclass cukup
 * mendaftarkan tombol lewat addButton(...) (tombol yang sudah tergambar di
 * background) atau addVisibleButton(...) (tombol digambar oleh kode).
 */
public abstract class BaseScreen implements ScreenMethod {

	protected final Game game;
	private final List<MyButton> buttons = new ArrayList<>();

	protected BaseScreen(Game game) {
		this.game = game;
	}

	// Path gambar background layar ini, atau null kalau subclass menggambar sendiri
	protected abstract String getBackground();

	protected MyButton addButton(String text, int x, int y, int width, int height, Runnable onClick) {
		MyButton button = new MyButton(text, x, y, width, height, () -> {
			game.getSound().play("click");
			onClick.run();
		});
		buttons.add(button);
		return button;
	}

	protected MyButton addVisibleButton(String text, int x, int y, int width, int height, Runnable onClick) {
		MyButton button = addButton(text, x, y, width, height, onClick);
		button.setVisible(true);
		return button;
	}

	// Hook untuk menggambar isi tambahan di atas background
	protected void renderContent(Graphics2D g) {
	}

	@Override
	public void render(Graphics2D g) {
		String background = getBackground();
		if (background != null) {
			g.drawImage(Assets.get(background), 0, 0, Board.WIDTH, Board.HEIGHT, null);
		}
		renderContent(g);
		for (MyButton button : buttons) {
			if (button.isVisible()) {
				button.draw(g);
			} else {
				button.drawHighlight(g);
			}
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

	// Musik menu; layar yang tidak butuh musik bisa override
	@Override
	public void onShow() {
		game.getSound().playMusic("music_day");
	}

	@Override
	public void onHide() {
		for (MyButton button : buttons) {
			button.resetBooleans();
		}
	}

}
