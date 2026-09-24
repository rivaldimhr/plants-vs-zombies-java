package game;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import entity.plant.PlantType;

public class Game extends JFrame implements Runnable {
	private static final long serialVersionUID = 1L;

	public static final int UPS = 60; // update (tick) per detik
	private static final double FPS_SET = 120.0;

	// Hasil permainan terakhir, ditampilkan di layar Win / Game Over
	public record GameResult(Level level, boolean won, int wavesSurvived, int zombiesKilled,
			List<PlantType> unlockedPlants, boolean newRecord) {
	}

	private final KeyHandler keyHandler = new KeyHandler();
	private final Deck deck = new Deck();
	private final SaveData saveData;
	private final SoundManager sound;
	private final Map<States, ScreenMethod> screens = new EnumMap<>(States.class);
	private final GameScreen gameScreen;
	private final GameLevel gameLevel;
	private volatile States states = States.MENU;
	private Level selectedLevel = Level.ADVENTURE.get(0);
	private GameResult lastResult;

	public Game() {
		saveData = SaveData.loadDefault();
		sound = new SoundManager(saveData);
		gameScreen = new GameScreen(this);
		gameLevel = new GameLevel(this);

		screens.put(States.MENU, new Menu(this));
		screens.put(States.LEVEL_SELECT, new LevelSelect(this));
		screens.put(States.INVENTORY, new Inventory(this));
		screens.put(States.GAME_LEVEL, gameLevel);
		screens.put(States.GAME_OVER, new GameOver(this));
		screens.put(States.WIN, new Win(this));
		screens.put(States.PLANTS_LIST, new PlantsList(this));
		screens.put(States.ZOMBIES_LIST, new ZombiesList(this));
		screens.put(States.HELP, new Help(this));
		screens.put(States.SETTINGS, new Settings(this));

		setTitle("Plants vs Zombies (Java)");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		add(gameScreen);
		gameScreen.setScale(saveData.getWindowScale());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		getCurrentScreen().onShow();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Game game = new Game();
			game.gameScreen.initInputs();
			game.start();
		});
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
				// kalau tertinggal jauh (misalnya laptop sleep), jangan kejar-kejaran
				if (now - lastUpdate > timePerUpdate * UPS) {
					lastUpdate = now;
				}
			}

			// Istirahat sebentar supaya CPU tidak 100%
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	// ------------------------------------------------------------------ alur permainan

	// Pilih level lalu ke layar pemilihan deck
	public void selectLevel(Level level) {
		selectedLevel = level;
		setStates(States.INVENTORY);
	}

	// Mulai permainan dengan level & deck yang sudah dipilih
	public void startNewGame() {
		gameLevel.start(selectedLevel);
		setStates(States.GAME_LEVEL);
	}

	// Dipanggil GameLevel saat permainan selesai
	public void finishLevel(Board board) {
		Level level = board.getLevel();
		int waves = board.getWaveSpawner().getWavesSpawned();
		if (board.getResult() == Board.Result.WON) {
			List<PlantType> unlocked = saveData.completeLevel(level);
			lastResult = new GameResult(level, true, waves, board.getZombiesKilled(), unlocked, false);
			setStates(States.WIN);
		} else {
			int survived = Math.max(0, waves - 1);
			boolean record = level.isEndless() && saveData.submitEndlessScore(survived);
			lastResult = new GameResult(level, false, survived, board.getZombiesKilled(), List.of(), record);
			setStates(States.GAME_OVER);
		}
	}

	public ScreenMethod getCurrentScreen() {
		return screens.get(states);
	}

	public States getStates() {
		return states;
	}

	public void setStates(States next) {
		ScreenMethod previous = getCurrentScreen();
		if (previous != null) {
			previous.onHide();
		}
		this.states = next;
		getCurrentScreen().onShow();
	}

	// Ubah ukuran jendela (1x, 1.5x, 2x)
	public void applyWindowScale(double scale) {
		saveData.setWindowScale(scale);
		SwingUtilities.invokeLater(() -> {
			gameScreen.setScale(scale);
			pack();
			setLocationRelativeTo(null);
		});
	}

	// Getters
	public KeyHandler getKeyHandler() {
		return keyHandler;
	}

	public Deck getDeck() {
		return deck;
	}

	public SaveData getSaveData() {
		return saveData;
	}

	public SoundManager getSound() {
		return sound;
	}

	public GameLevel getGameLevel() {
		return gameLevel;
	}

	public GameScreen getGameScreen() {
		return gameScreen;
	}

	public Level getSelectedLevel() {
		return selectedLevel;
	}

	public GameResult getLastResult() {
		return lastResult;
	}

}
