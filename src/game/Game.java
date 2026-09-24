package game;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.JFrame;

public class Game extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;

	public static final int UPS = 60; // update (tick) per detik
	private static final double FPS_SET = 120.0;

	private final KeyHandler keyHandler = new KeyHandler();
	private final Deck deck = new Deck();
	private final Map<States, ScreenMethod> screens = new EnumMap<>(States.class);
	private final GameScreen gameScreen;
	private final GameLevel gameLevel;
	private volatile States states = States.MENU;

	public Game() {
		gameScreen = new GameScreen(this);
		gameLevel = new GameLevel(this);

		screens.put(States.MENU, new Menu(this));
		screens.put(States.PLANTS_LIST, new PlantsList(this));
		screens.put(States.ZOMBIES_LIST, new ZombiesList(this));
		screens.put(States.HELP, new Help(this));
		screens.put(States.INVENTORY, new Inventory(this));
		screens.put(States.GAME_LEVEL, gameLevel);
		screens.put(States.GAME_OVER, new GameOver(this));
		screens.put(States.WIN, new Win(this));

		setTitle("Plants vs Zombies");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		add(gameScreen);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args) {
		Game game = new Game();
		game.gameScreen.initInputs();
		game.start();
	}

	private void start() {
		Thread gameThread = new Thread(this, "game-loop");
		gameThread.setDaemon(true);
		gameThread.start();
	}

	@Override
	public void run() {
		double timePerFrame = 1000000000.0 / FPS_SET;
		double timePerUpdate = 1000000000.0 / UPS;

		long lastFrame = System.nanoTime();
		long lastUpdate = System.nanoTime();

		while (true) {
			long now = System.nanoTime();

			// Render
			if (now - lastFrame >= timePerFrame) {
				gameScreen.repaint();
				lastFrame = now;
			}

			// Update
			if (now - lastUpdate >= timePerUpdate) {
				getCurrentScreen().update();
				// ditambah interval (bukan = now) supaya keterlambatan tidak menumpuk
				lastUpdate += timePerUpdate;
			}

			// Istirahat sebentar supaya CPU tidak 100%
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	// Mulai permainan baru dengan deck yang sudah dipilih
	public void startNewGame() {
		gameLevel.reset();
		setStates(States.GAME_LEVEL);
	}

	public ScreenMethod getCurrentScreen() {
		return screens.get(states);
	}

	// Getters and setters
	public States getStates() {
		return states;
	}

	public void setStates(States states) {
		ScreenMethod previous = getCurrentScreen();
		if (previous instanceof BaseScreen) {
			((BaseScreen) previous).resetButtons();
		}
		this.states = states;
	}

	public KeyHandler getKeyHandler() {
		return keyHandler;
	}

	public Deck getDeck() {
		return deck;
	}

	public GameLevel getGameLevel() {
		return gameLevel;
	}

	public GameScreen getGameScreen() {
		return gameScreen;
	}

}
