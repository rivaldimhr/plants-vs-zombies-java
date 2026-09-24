package game;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import entity.plant.Plant;
import entity.plant.PlantType;

// Almanac tanaman
public class PlantsList extends AlmanacScreen {
	private static final PlantType[] TYPES = PlantType.values();
	private static final int COLS = 5, CARD_W = 54, CARD_H = 72, GAP_X = 8, GAP_Y = 10;

	public PlantsList(Game game) {
		super(game);
		registerEntries();
	}

	@Override
	protected String title() {
		return "ALMANAC TANAMAN";
	}

	@Override
	protected int entryCount() {
		return TYPES.length;
	}

	@Override
	protected Rectangle entryBounds(int index) {
		int x = GRID_PANEL.x + 16 + (index % COLS) * (CARD_W + GAP_X);
		int y = GRID_PANEL.y + 18 + (index / COLS) * (CARD_H + GAP_Y);
		return new Rectangle(x, y, CARD_W, CARD_H);
	}

	@Override
	protected void drawEntry(Graphics2D g, int index, Rectangle r) {
		g.drawImage(Assets.get(TYPES[index].getCardImage(), r.width, r.height), r.x, r.y, null);
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
		PlantType type = TYPES[index];
		Plant plant = type.getPrototype();
		List<String> stats = new ArrayList<>();
		stats.add("Cost: " + plant.getCost() + " sun    Cooldown: " + plant.getCooldown() + " detik");
		stats.add("Health: " + plant.getHealth());
		if (plant.getAttackDamage() > 0) {
			String speed = plant.getAttackSpeed() > 0 ? "  tiap " + format(plant.getAttackSpeed()) + " detik" : "";
			stats.add("Damage: " + plant.getAttackDamage() + speed);
			stats.add("Range: " + range(plant.getRange()));
		}
		stats.add("Area: " + (type.isAquatic() ? "kolam" : "darat") + (type.isNocturnal() ? ", tidur saat siang" : ""));
		return stats;
	}

	private static String range(int range) {
		return range == -1 ? "satu baris" : range + " tile";
	}

	private static String format(double value) {
		return value == Math.floor(value) ? Integer.toString((int) value) : Double.toString(value);
	}
}
