package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import entity.plant.PlantType;

// Pilih level petualangan (terbuka berurutan) atau mode Endless
public class LevelSelect extends BaseScreen {
	private static final int CARD_W = 190, CARD_H = 104;

	private final List<MyButton> levelButtons = new ArrayList<>();
	private final MyButton endlessButton;

	public LevelSelect(Game game) {
		super(game);
		for (Level level : Level.ADVENTURE) {
			Rectangle r = cardBounds(level.getIndex());
			levelButtons.add(addButton(level.getTitle(), r.x, r.y, r.width, r.height, () -> game.selectLevel(level)));
		}
		endlessButton = addVisibleButton("Endless", 400, 368, 120, 32, () -> game.selectLevel(Level.ENDLESS));
		addVisibleButton("Menu", 530, 368, 110, 32, () -> game.setStates(States.MENU));
	}

	private static Rectangle cardBounds(int index) {
		int col = index % 3;
		int row = index / 3;
		return new Rectangle(28 + col * (CARD_W + 12), 70 + row * (CARD_H + 12), CARD_W, CARD_H);
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/BACKGROUND DAY.png";
	}

	@Override
	public void onShow() {
		super.onShow();
		SaveData save = game.getSaveData();
		for (Level level : Level.ADVENTURE) {
			levelButtons.get(level.getIndex()).setEnabled(save.isLevelUnlocked(level));
		}
		endlessButton.setEnabled(save.isLevelUnlocked(Level.ENDLESS));
	}

	@Override
	protected void renderContent(Graphics2D g) {
		UI.dim(g, 110);
		UI.outlinedTitle(g, "PILIH LEVEL", Board.WIDTH / 2, 50, 30, new Color(255, 230, 120), UI.WOOD_DARK);
		SaveData save = game.getSaveData();
		for (Level level : Level.ADVENTURE) {
			drawLevelCard(g, level, cardBounds(level.getIndex()), save);
		}
		String best = save.isLevelUnlocked(Level.ENDLESS)
				? "Endless - rekor: " + save.getEndlessBest() + " wave"
				: "Endless terbuka setelah Level " + SaveData.ENDLESS_UNLOCK_LEVEL + " selesai";
		UI.shadowText(g, best, 28, 390, Color.WHITE);
	}

	private void drawLevelCard(Graphics2D g, Level level, Rectangle r, SaveData save) {
		boolean unlocked = save.isLevelUnlocked(level);
		boolean done = save.isLevelCompleted(level);
		UI.woodPanel(g, r.x, r.y, r.width, r.height);
		UI.paperPanel(g, r.x + 8, r.y + 8, r.width - 16, r.height - 16);

		g.setFont(UI.font(15));
		g.setColor(UI.WOOD_DARK);
		g.drawString("Level " + level.getNumber(), r.x + 16, r.y + 28);
		g.setFont(UI.font(12));
		g.drawString(level.getName(), r.x + 16, r.y + 45);
		g.setFont(UI.plain(11));
		g.setColor(level.isNight() ? new Color(60, 60, 150) : new Color(170, 110, 0));
		g.drawString((level.isNight() ? "Malam" : "Siang") + " - " + level.getWaveCount() + " wave", r.x + 16, r.y + 61);

		// tanaman hadiah
		List<PlantType> rewards = level.getRewards();
		g.setColor(UI.WOOD_DARK);
		g.drawString(rewards.isEmpty() ? "Level terakhir" : "Hadiah:", r.x + 16, r.y + 80);
		int cx = r.x + 62;
		for (PlantType reward : rewards) {
			Image card = Assets.get(reward.getCardImage());
			g.drawImage(card, cx, r.y + 66, 17, 24, null);
			cx += 20;
		}

		if (done) {
			UI.centered(g, "SELESAI", r.x + r.width - 42, r.y + 30, UI.font(12), new Color(60, 170, 60));
		}
		if (!unlocked) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRoundRect(r.x, r.y, r.width, r.height, 18, 18);
			UI.centered(g, "TERKUNCI", r.x + r.width / 2, r.y + r.height / 2 + 6, UI.font(16), new Color(230, 230, 230));
		}
	}
}
