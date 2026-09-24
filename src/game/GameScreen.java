package game;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * Panel tempat layar aktif digambar. Game selalu digambar di resolusi 660x420
 * lalu diperbesar sesuai skala jendela (1x / 1.5x / 2x).
 */
public class GameScreen extends JPanel {
	private static final long serialVersionUID = 1L;

	private final Game game;
	private volatile double scale = 1.0;

	public GameScreen(Game game) {
		this.game = game;
		setScale(1.0);
	}

	public void setScale(double scale) {
		this.scale = scale;
		Dimension size = new Dimension((int) Math.round(Board.WIDTH * scale), (int) Math.round(Board.HEIGHT * scale));
		setMinimumSize(size);
		setPreferredSize(size);
		setMaximumSize(size);
		revalidate();
	}

	public double getScale() {
		return scale;
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
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.scale(scale, scale);
		g2.clipRect(0, 0, Board.WIDTH, Board.HEIGHT);
		game.getCurrentScreen().render(g2);
	}

}
