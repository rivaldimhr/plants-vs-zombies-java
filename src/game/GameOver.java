package game;

import java.awt.Color;
import java.awt.Graphics2D;

// Layar kalah: "THE ZOMBIES ATE YOUR BRAINS!" + skor
public class GameOver extends BaseScreen {

	public GameOver(Game game) {
		super(game);
		addVisibleButton("Coba Lagi", 140, 372, 120, 34, game::startNewGame);
		addVisibleButton("Pilih Level", 270, 372, 120, 34, () -> game.setStates(States.LEVEL_SELECT));
		addVisibleButton("Menu", 400, 372, 120, 34, () -> game.setStates(States.MENU));
	}

	@Override
	protected String getBackground() {
		return null;
	}

	@Override
	public void onShow() {
		game.getSound().stopMusic();
	}

	@Override
	protected void renderContent(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, Board.WIDTH, Board.HEIGHT);
		// gameOver.jpg 1111x602: diperkecil selebar layar
		g.drawImage(Assets.get("image/ui/gameOver.jpg", Board.WIDTH, 357), 0, 0, null);
		Game.GameResult result = game.getLastResult();
		if (result == null) {
			return;
		}
		String score = result.level().isEndless() ? result.wavesSurvived() + " wave"
				: Integer.toString(result.zombiesKilled());
		g.setFont(UI.font(20));
		UI.shadowText(g, score, 560, 306, new Color(120, 230, 90));
		String info = result.level().isEndless()
				? "Rekor Endless: " + game.getSaveData().getEndlessBest() + " wave" + (result.newRecord() ? "  (REKOR BARU!)" : "")
				: result.level().getTitle() + " - bertahan " + result.wavesSurvived() + " wave, "
						+ result.zombiesKilled() + " zombie dikalahkan";
		UI.centered(g, info, Board.WIDTH / 2, 352, UI.font(12), Color.WHITE);
	}

}
