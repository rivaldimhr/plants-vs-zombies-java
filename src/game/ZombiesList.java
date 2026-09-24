package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import entity.zombie.Armor;
import entity.zombie.Zombie;
import entity.zombie.ZombieType;

// Almanac zombie
public class ZombiesList extends AlmanacScreen {
	private static final ZombieType[] TYPES = ZombieType.values();
	private static final int COLS = 4, SIZE = 70, GAP = 8;

	public ZombiesList(Game game) {
		super(game);
		registerEntries();
	}

	@Override
	protected String title() {
		return "ALMANAC ZOMBIE";
	}

	@Override
	protected int entryCount() {
		return TYPES.length;
	}

	@Override
	protected Rectangle entryBounds(int index) {
		int x = GRID_PANEL.x + 16 + (index % COLS) * (SIZE + GAP);
		int y = GRID_PANEL.y + 18 + (index / COLS) * (SIZE + GAP);
		return new Rectangle(x, y, SIZE, SIZE);
	}

	@Override
	protected void drawEntry(Graphics2D g, int index, Rectangle r) {
		g.setColor(TYPES[index].isAquatic() ? new Color(90, 150, 200) : new Color(110, 160, 80));
		g.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
		g.setColor(UI.WOOD_DARK);
		g.drawRoundRect(r.x, r.y, r.width, r.height, 8, 8);
		BufferedImage frame = entrySprite(index).frame(0);
		double scale = Math.min((r.width - 6.0) / frame.getWidth(), (r.height - 4.0) / frame.getHeight());
		int w = (int) (frame.getWidth() * scale);
		int h = (int) (frame.getHeight() * scale);
		g.drawImage(frame, r.x + (r.width - w) / 2, r.y + r.height - h - 2, w, h, null);
	}

	@Override
	protected String entryName(int index) {
		return TYPES[index].getDisplayName();
	}

	@Override
	protected String entryDescription(int index) {
		return TYPES[index].getDescription();
	}

	@Override
	protected Sprite entrySprite(int index) {
		return TYPES[index].getPrototype().getSprite();
	}

	@Override
	protected List<String> entryStats(int index) {
		Zombie zombie = TYPES[index].getPrototype();
		List<String> stats = new ArrayList<>();
		Armor armor = zombie.getArmor();
		String armorText = armor == null ? "" : " (badan " + zombie.getHealth() + " + " + armorName(armor) + " "
				+ armor.getMaxHealth() + ")";
		stats.add("Health: " + zombie.getTotalHealth() + armorText);
		stats.add("Damage: " + zombie.getAttackDamage() + " tiap " + (int) zombie.getAttackSpeed() + " detik");
		double speed = (double) Game.UPS / (zombie.getWalkDelay() + 1);
		stats.add(String.format("Kecepatan: %.1f px/detik", speed));
		stats.add("Area: " + (zombie.isAquatic() ? "kolam" : "darat"));
		return stats;
	}

	private static String armorName(Armor armor) {
		switch (armor.getKind()) {
			case CONE:
				return "cone";
			case BUCKET:
				return "ember";
			case PAPER:
				return "koran";
			default:
				return "helm";
		}
	}
}
