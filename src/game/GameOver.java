package game;

// Layar kalah: Play Again = pilih deck lagi, Exit = kembali ke menu
public class GameOver extends BaseScreen {

	public GameOver(Game game) {
		super(game);
		addButton("Play Again", 194, 327, 127, 45, () -> game.setStates(States.INVENTORY));
		addButton("Exit", 353, 327, 127, 45, () -> game.setStates(States.MENU));
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/GAME OVER.png";
	}

}
