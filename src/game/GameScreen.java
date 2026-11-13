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

	// -Dpvz.fastRender=true: gambar tanpa interpolasi & mode kualitas (jauh lebih ringan untuk versi browser)
	private static final boolean FAST_RENDER = Boolean.getBoolean("pvz.fastRender");
	private volatile long frames = 0;
	private volatile long maxPaintNanos = 0;
	private volatile States lastPaintedState = null;

	public long getFrameCount() {
		return frames;
	}

	// Waktu menggambar terlama sejak terakhir dipanggil (untuk log kinerja)
	public long takeMaxPaintMillis() {
		long ms = maxPaintNanos / 1_000_000;
		maxPaintNanos = 0;
		return ms;
	}

	@Override
	public void paintComponent(Graphics g) {
		long t0 = System.nanoTime();
		super.paintComponent(g);
		frames++;
		Graphics2D g2 = (Graphics2D) g;
		if (FAST_RENDER) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		} else {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		}
		g2.scale(scale, scale);
		g2.clipRect(0, 0, Board.WIDTH, Board.HEIGHT);
		States state = game.getStates();
		game.getCurrentScreen().render(g2);
		long took = System.nanoTime() - t0;
		maxPaintNanos = Math.max(maxPaintNanos, took);
		if (Game.PERF_LOG && state != lastPaintedState) {
			System.out.printf("PVZ_FIRSTPAINT state=%s ms=%d%n", state, took / 1_000_000);
			lastPaintedState = state;
		}
	}

}
