package game;

import java.awt.Graphics2D;

// Satu layar game (menu, inventory, permainan, dst). Game memanggil layar yang sedang aktif.
public interface ScreenMethod {

	void render(Graphics2D g);

	void mouseClicked(int x, int y);

	void mouseMoved(int x, int y);

	void mousePressed(int x, int y);

	void mouseReleased(int x, int y);

	// Klik kanan; default tidak melakukan apa-apa
	default void rightClicked(int x, int y) {
	}

	// Mouse digerakkan sambil ditekan; default sama seperti mouseMoved
	default void mouseDragged(int x, int y) {
		mouseMoved(x, y);
	}

	// Dipanggil tiap tick selama layar ini aktif; default tidak melakukan apa-apa
	default void update() {
	}

}
