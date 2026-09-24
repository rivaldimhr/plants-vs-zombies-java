package game;

// Daftar tanaman: klik kartu untuk melihat deskripsi & stat
public class PlantsList extends CatalogScreen {

	public PlantsList(Game game) {
		super(game);
		addEntry(58, 105, 60, 80, "image/IMAGE/PEASHOOTER DECK.png");
		addEntry(150, 105, 60, 80, "image/IMAGE/SUNFLOWER DECK.png");
		addEntry(241, 105, 60, 80, "image/IMAGE/WALL-NUT DECK.png");
		addEntry(334, 105, 60, 80, "image/IMAGE/SNOW PEA DECK.png");
		addEntry(58, 198, 60, 80, "image/IMAGE/SQUASH DECK.png");
		addEntry(150, 198, 60, 80, "image/IMAGE/LILY PAD DECK.png");
		addEntry(241, 198, 60, 80, "image/IMAGE/TALL-NUT DECK.png");
		addEntry(335, 198, 60, 80, "image/IMAGE/PUFF-SHROOM DECK.png");
		addEntry(58, 295, 60, 80, "image/IMAGE/TANGLE KELP DECK.png");
		addEntry(150, 293, 60, 80, "image/IMAGE/REPEATER DECK.png");
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/PLANT.png";
	}

}
