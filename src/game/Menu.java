package game;

// Menu utama: tombol Start/Plants/Zombies/Help/Exit sudah tergambar di background
public class Menu extends BaseScreen {

	public Menu(Game game) {
		super(game);
		int w = 115;
		int h = 40;
		addButton("Start", 270, 303, w, h, () -> game.setStates(States.LEVEL_SELECT));
		addButton("Plants", 175, 241, w, h, () -> game.setStates(States.PLANTS_LIST));
		addButton("Zombies", 363, 240, w, h, () -> game.setStates(States.ZOMBIES_LIST));
		addButton("Help", 175, 360, w, h, () -> game.setStates(States.HELP));
		addButton("Exit", 363, 360, w, h, () -> System.exit(0));
		addVisibleButton("Pengaturan", 8, 8, 110, 26, () -> game.setStates(States.SETTINGS));
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/MENU.png";
	}

}
