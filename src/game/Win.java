package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;
import java.util.stream.Collectors;

import entity.plant.PlantType;

// Layar menang: info level + tanaman baru yang terbuka
public class Win extends BaseScreen {

	public Win(Game game) {
		super(game);
		// tombol digambar ulang di atas tombol bawaan gambar WIN.png
		addVisibleButton("Level Berikut", 194, 327, 127, 45, this::next);
		addVisibleButton("Menu", 353, 327, 127, 45, () -> game.setStates(States.MENU));
	}

	private void next() {
		Game.GameResult result = game.getLastResult();
		Level next = result == null ? null : result.level().next();
		if (next != null) {
			game.selectLevel(next);
		} else {
			game.setStates(States.LEVEL_SELECT);
		}
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/WIN.png";
	}

	@Override
	public void onShow() {
		game.getSound().stopMusic();
	}

	@Override
	protected void renderContent(Graphics2D g) {
		Game.GameResult result = game.getLastResult();
		if (result == null) {
			return;
		}
		UI.centered(g, result.level().getTitle() + " selesai!  " + result.zombiesKilled() + " zombie dikalahkan",
				Board.WIDTH / 2, 282, UI.font(13), Color.WHITE);
		List<PlantType> unlocked = result.unlockedPlants();
		if (!unlocked.isEmpty()) {
			String names = unlocked.stream().map(PlantType::getDisplayName).collect(Collectors.joining(" & "));
			UI.centered(g, "Tanaman baru: " + names + "!", Board.WIDTH / 2, 312, UI.font(15), new Color(255, 230, 90));
			int x = Board.WIDTH / 2 - unlocked.size() * 22;
			for (PlantType type : unlocked) {
				g.drawImage(Assets.get(type.getCardImage()), x, 380, 30, 40, null);
				x += 44;
			}
		} else if (result.level().next() == null) {
			UI.centered(g, "Selamat! Semua level selesai. Coba mode Endless!", Board.WIDTH / 2, 312, UI.font(14),
					new Color(255, 230, 90));
		}
	}

}
