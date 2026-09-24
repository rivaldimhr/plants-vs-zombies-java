package game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Meneruskan event mouse ke layar yang sedang aktif
public class MyMouseListener implements MouseListener, MouseMotionListener {

	private final Game game;

	public MyMouseListener(Game game) {
		this.game = game;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		game.getCurrentScreen().mouseDragged(e.getX(), e.getY());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		game.getCurrentScreen().mouseMoved(e.getX(), e.getY());
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			game.getCurrentScreen().mouseClicked(e.getX(), e.getY());
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// supaya keyboard tetap jalan setelah klik
		e.getComponent().requestFocusInWindow();
		if (e.getButton() == MouseEvent.BUTTON1) {
			game.getCurrentScreen().mousePressed(e.getX(), e.getY());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			game.getCurrentScreen().rightClicked(e.getX(), e.getY());
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			game.getCurrentScreen().mouseReleased(e.getX(), e.getY());
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
