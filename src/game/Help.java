package game;

import java.awt.Color;
import java.awt.Graphics2D;

// Petunjuk permainan (gambar HELP.png) + panel kontrol
public class Help extends BaseScreen {
	private static final String[][] CONTROLS = {
			{ "Klik sun", "ambil sun" },
			{ "Klik kartu, klik tile", "menanam" },
			{ "Klik sekop, klik tanaman", "mencabut tanaman" },
			{ "Klik kanan", "batal memilih" },
			{ "Panah + Enter", "pilih tile dengan keyboard" },
			{ "1 - 6", "tanam kartu ke tile terpilih" },
			{ "7", "sekop di tile terpilih" },
			{ "F", "kecepatan 1x / 2x" },
			{ "P / Esc", "pause" },
	};

	private boolean showControls = false;
	private final MyButton controlsButton;

	public Help(Game game) {
		super(game);
		addButton("Menu", 555, 15, 75, 22, () -> {
			showControls = false;
			game.setStates(States.MENU);
		});
		controlsButton = addVisibleButton("Kontrol", 470, 14, 80, 24, () -> showControls = !showControls);
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/HELP.png";
	}

	@Override
	protected void renderContent(Graphics2D g) {
		controlsButton.setText(showControls ? "Tutup" : "Kontrol");
		if (!showControls) {
			return;
		}
		UI.dim(g, 120);
		UI.woodPanel(g, 110, 60, 440, 320);
		UI.paperPanel(g, 125, 100, 410, 265);
		UI.centered(g, "KONTROL", Board.WIDTH / 2, 90, UI.font(20), new Color(255, 230, 120));
		int y = 128;
		for (String[] row : CONTROLS) {
			g.setFont(UI.font(13));
			g.setColor(UI.WOOD_DARK);
			g.drawString(row[0], 140, y);
			g.setFont(UI.plain(13));
			g.drawString(row[1], 340, y);
			y += 26;
		}
	}

}
