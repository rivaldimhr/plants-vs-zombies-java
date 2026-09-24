package game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Meneruskan event mouse ke layar yang sedang aktif (koordinat dikembalikan ke skala 1x)
public class MyMouseListener implements MouseListener, MouseMotionListener {

	private final Game game;

	public MyMouseListener(Game game) {
		this.game = game;
	}

	private int gx(MouseEvent e) {
		return (int) (e.getX() / game.getGameScreen().getScale());
	}

	private int gy(MouseEvent e) {
		return (int) (e.getY() / game.getGameScreen().getScale());
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		game.getCurrentScreen().mouseDragged(gx(e), gy(e));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		game.getCurrentScreen().mouseMoved(gx(e), gy(e));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			game.getCurrentScreen().mouseClicked(gx(e), gy(e));
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// supaya keyboard tetap jalan setelah klik
		e.getComponent().requestFocusInWindow();
		if (e.getButton() == MouseEvent.BUTTON1) {
			game.getCurrentScreen().mousePressed(gx(e), gy(e));
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			game.getCurrentScreen().rightClicked(gx(e), gy(e));
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			game.getCurrentScreen().mouseReleased(gx(e), gy(e));
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
