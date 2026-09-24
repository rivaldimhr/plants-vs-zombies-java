package game;

// Layar menang: Play Again = pilih deck lagi, Exit = kembali ke menu
public class Win extends BaseScreen {

	public Win(Game game) {
		super(game);
		addButton("Play Again", 194, 327, 127, 45, () -> game.setStates(States.INVENTORY));
		addButton("Exit", 353, 327, 127, 45, () -> game.setStates(States.MENU));
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/WIN.png";
	}

}
