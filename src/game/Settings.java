package game;

import java.awt.Color;
import java.awt.Graphics2D;

// Pengaturan: suara, musik, ukuran jendela, buka semua level (demo), reset progres
public class Settings extends BaseScreen {
	private static final double[] SCALES = { 1.0, 1.5, 2.0 };

	private final MyButton soundButton;
	private final MyButton musicButton;
	private final MyButton scaleButton;
	private final MyButton unlockButton;
	private final MyButton resetButton;
	private boolean confirmReset = false;
	private final Toast toast = new Toast();

	public Settings(Game game) {
		super(game);
		int x = 330, w = 150, h = 30;
		soundButton = addVisibleButton("", x, 110, w, h, () -> {
			SaveData save = game.getSaveData();
			save.setSoundOn(!save.isSoundOn());
		});
		musicButton = addVisibleButton("", x, 150, w, h, () -> {
			SaveData save = game.getSaveData();
			save.setMusicOn(!save.isMusicOn());
			game.getSound().refreshMusic();
		});
		scaleButton = addVisibleButton("", x, 190, w, h, this::nextScale);
		unlockButton = addVisibleButton("", x, 230, w, h, () -> {
			SaveData save = game.getSaveData();
			save.setUnlockAll(!save.isUnlockAll());
		});
		resetButton = addVisibleButton("", x, 270, w, h, this::reset);
		addVisibleButton("Kembali", 255, 330, 150, 34, () -> game.setStates(States.MENU));
	}

	private void nextScale() {
		double current = game.getSaveData().getWindowScale();
		double next = SCALES[0];
		for (int i = 0; i < SCALES.length; i++) {
			if (Math.abs(SCALES[i] - current) < 0.01) {
				next = SCALES[(i + 1) % SCALES.length];
			}
		}
		game.applyWindowScale(next);
	}

	private void reset() {
		if (!confirmReset) {
			confirmReset = true;
			toast.show("Klik sekali lagi untuk menghapus semua progres");
			return;
		}
		confirmReset = false;
		game.getSaveData().resetProgress();
		toast.show("Progres dihapus");
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/BACKGROUND DAY.png";
	}

	@Override
	public void onShow() {
		super.onShow();
		confirmReset = false;
	}

	@Override
	protected void renderContent(Graphics2D g) {
		SaveData save = game.getSaveData();
		soundButton.setText(save.isSoundOn() ? "ON" : "OFF");
		musicButton.setText(save.isMusicOn() ? "ON" : "OFF");
		scaleButton.setText(format(save.getWindowScale()) + "x");
		unlockButton.setText(save.isUnlockAll() ? "ON" : "OFF");
		resetButton.setText(confirmReset ? "Yakin?" : "Reset");

		UI.dim(g, 110);
		UI.woodPanel(g, 150, 50, 360, 330);
		UI.outlinedTitle(g, "PENGATURAN", Board.WIDTH / 2, 92, 26, new Color(255, 230, 120), UI.WOOD_DARK);
		String[] labels = { "Efek suara", "Musik", "Ukuran jendela", "Buka semua level (demo)", "Hapus progres" };
		for (int i = 0; i < labels.length; i++) {
			g.setFont(UI.font(13));
			UI.shadowText(g, labels[i], 170, 130 + i * 40, Color.WHITE);
		}
		toast.draw(g, 400);
	}

	private static String format(double scale) {
		return scale == Math.floor(scale) ? Integer.toString((int) scale) : Double.toString(scale);
	}

}
