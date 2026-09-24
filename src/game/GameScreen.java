package game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

// Panel tempat semua layar digambar
public class GameScreen extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Game game;

	public GameScreen(Game game) {
		this.game = game;
		Dimension size = new Dimension(Board.WIDTH, Board.HEIGHT);
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		Assets.setObserver(this);
	}

	public void initInputs() {
		MyMouseListener myMouseListener = new MyMouseListener(game);
		addMouseListener(myMouseListener);
		addMouseMotionListener(myMouseListener);
		addKeyListener(game.getKeyHandler());
		setFocusable(true);
		requestFocusInWindow();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		game.getCurrentScreen().render(g2);
	}

}
