package game;

import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Kelas dasar Plants List dan Zombies List: klik kartu untuk menampilkan
 * deskripsinya, klik tombol Menu untuk kembali.
 */
public abstract class CatalogScreen extends BaseScreen {

	private Image selectedDescription = null;

	protected CatalogScreen(Game game) {
		super(game);
		addButton("Menu", 555, 15, 75, 22, () -> {
			selectedDescription = null;
			game.setStates(States.MENU);
		});
	}

	// Satu kartu: area klik + gambar deskripsi (gambar layar penuh)
	protected void addEntry(int x, int y, int width, int height, String descriptionImage) {
		addButton(descriptionImage, x, y, width, height, () -> selectedDescription = Assets.get(descriptionImage));
	}

	@Override
	protected void renderContent(Graphics2D g) {
		if (selectedDescription != null) {
			g.drawImage(selectedDescription, 0, 0, null);
		}
	}
}
