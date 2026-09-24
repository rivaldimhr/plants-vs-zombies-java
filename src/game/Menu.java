package game;

public class Menu extends BaseScreen {

	public Menu(Game game) {
		super(game);
		int w = 115;
		int h = 40;
		addButton("Play", 270, 303, w, h, () -> game.setStates(States.INVENTORY));
		addButton("Plants List", 175, 241, w, h, () -> game.setStates(States.PLANTS_LIST));
		addButton("Zombies List", 363, 240, w, h, () -> game.setStates(States.ZOMBIES_LIST));
		addButton("Help", 175, 360, w, h, () -> game.setStates(States.HELP));
		addButton("Quit", 363, 360, w, h, () -> System.exit(0));
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/MENU.png";
	}

}
