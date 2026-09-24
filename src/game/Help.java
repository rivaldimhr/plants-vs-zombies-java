package game;

public class Help extends BaseScreen {

	public Help(Game game) {
		super(game);
		addButton("Menu", 555, 15, 75, 22, () -> game.setStates(States.MENU));
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/HELP.png";
	}

}
