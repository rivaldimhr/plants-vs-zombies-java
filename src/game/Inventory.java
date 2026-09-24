package game;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import entity.plant.PlantType;

/**
 * Layar pemilihan deck (maksimal 6 tanaman) sebelum level dimulai.
 * - Klik kartu inventory: masukkan ke / keluarkan dari deck
 * - Klik kartu deck: pilih untuk swap. Lalu klik kartu deck lain untuk menukar posisi,
 *   atau klik kartu inventory (yang belum ada di deck) untuk mengganti slot itu
 * - Klik kanan kartu deck: keluarkan dari deck
 * - Drag and drop: seret kartu deck ke slot lain (swap) atau keluar dari deck (hapus),
 *   seret tanaman inventory ke slot deck (ganti / isi slot kosong)
 * Tanaman yang belum terbuka tampil terkunci.
 */
public class Inventory extends BaseScreen {
	// Grid inventory: 3 baris x 5 kolom
	private static final int GRID_X = 92, GRID_Y = 148, GRID_DX = 84, GRID_DY = 80;
	private static final int CARD_W = 66, CARD_H = 76;
	private static final int COLS = 5;
	// Kartu deck (baris atas)
	private static final int DECK_SLOT = 90;
	private static final int DRAG_THRESHOLD = 6; // pixel sebelum dianggap drag, bukan klik

	private final Deck deck;
	private final Toast toast = new Toast();
	private volatile int selectedSlot = -1; // kartu deck yang dipilih untuk swap

	// Drag and drop
	private int pressX, pressY;
	private int dragSlot = -1; // sumber drag dari deck
	private PlantType dragType = null; // tanaman yang sedang diseret
	private boolean dragging = false;
	private boolean suppressClick = false;
	private int mouseX, mouseY;

	public Inventory(Game game) {
		super(game);
		this.deck = game.getDeck();
		addButton("Play", 570, 10, 80, 30, this::start);
		addButton("Clear", 570, 50, 80, 30, () -> {
			deck.clear();
			selectedSlot = -1;
			toast.show("Deck dikosongkan");
		});
		addVisibleButton("Kembali", 560, 385, 90, 28, () -> game.setStates(States.LEVEL_SELECT));
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/INVENTORY (7).png";
	}

	@Override
	public void onShow() {
		super.onShow();
		selectedSlot = -1;
		// buang tanaman yang terkunci (misalnya setelah progres di-reset)
		for (PlantType type : deck.getCards()) {
			if (!isUnlocked(type)) {
				deck.toggle(type);
			}
		}
		if (game.getSelectedLevel().isNight()) {
			toast.show("Level malam: sun tidak jatuh dari langit, jamur aktif");
		}
	}

	private boolean isUnlocked(PlantType type) {
		return game.getSaveData().isPlantUnlocked(type);
	}

	// Jumlah kartu yang harus dipilih: 6, atau semua tanaman kalau yang terbuka < 6
	private int requiredCards() {
		return Math.min(Deck.SIZE, game.getSaveData().getUnlockedPlants().size());
	}

	private void start() {
		int required = requiredCards();
		if (deck.size() < required) {
			toast.show("Deck belum lengkap, pilih " + required + " tanaman! (" + deck.size() + "/" + required + ")");
			return;
		}
		toast.clear();
		selectedSlot = -1;
		game.startNewGame();
	}

	private static Rectangle gridCard(int index) {
		return new Rectangle(GRID_X + GRID_DX * (index % COLS), GRID_Y + GRID_DY * (index / COLS), CARD_W, CARD_H);
	}

	private static Rectangle deckCard(int slot) {
		return new Rectangle(slot * DECK_SLOT, 0, DECK_SLOT, DECK_SLOT);
	}

	// Slot deck (yang terisi) di posisi (x, y), atau -1
	private int deckSlotAt(int x, int y) {
		for (int slot = 0; slot < deck.size(); slot++) {
			if (deckCard(slot).contains(x, y)) {
				return slot;
			}
		}
		return -1;
	}

	// Slot deck tujuan drop (termasuk slot kosong di baris deck), atau -1
	private static int dropSlotAt(int x, int y) {
		if (y >= 0 && y < DECK_SLOT && x >= 0 && x < Deck.SIZE * DECK_SLOT) {
			return x / DECK_SLOT;
		}
		return -1;
	}

	private PlantType inventoryCardAt(int x, int y) {
		PlantType[] types = PlantType.values();
		for (int i = 0; i < types.length; i++) {
			if (gridCard(i).contains(x, y)) {
				return types[i];
			}
		}
		return null;
	}

	private void toggle(PlantType type) {
		boolean wasInDeck = deck.contains(type);
		if (!wasInDeck && deck.size() >= Deck.SIZE) {
			toast.show("Deck sudah penuh. Klik kartu deck lalu kartu ini untuk mengganti");
			return;
		}
		deck.toggle(type);
		if (wasInDeck) {
			toast.show(type.getDisplayName() + " dikeluarkan dari deck");
		}
	}

	private void clickDeckSlot(int slot) {
		if (selectedSlot == -1) {
			selectedSlot = slot;
			toast.show("Klik kartu deck lain untuk menukar, atau tanaman di inventory untuk mengganti");
		} else if (selectedSlot == slot) {
			selectedSlot = -1;
		} else {
			deck.swap(selectedSlot, slot);
			toast.show("Posisi " + (selectedSlot + 1) + " dan " + (slot + 1) + " ditukar");
			selectedSlot = -1;
		}
	}

	private void clickInventoryCard(PlantType type) {
		if (!isUnlocked(type)) {
			toast.show("Tanaman ini terbuka setelah menyelesaikan level tertentu");
			return;
		}
		if (selectedSlot == -1) {
			toggle(type);
			return;
		}
		int slot = selectedSlot;
		selectedSlot = -1;
		int existing = deck.indexOf(type);
		if (existing == slot) {
			return;
		}
		if (existing >= 0) {
			deck.swap(slot, existing); // tanaman sudah di deck: tukar posisinya
			toast.show("Posisi " + (slot + 1) + " dan " + (existing + 1) + " ditukar");
		} else {
			PlantType old = deck.get(slot);
			deck.replace(slot, type);
			toast.show(old.getDisplayName() + " diganti " + type.getDisplayName());
		}
	}

	// ------------------------------------------------------------------ mouse

	@Override
	public void mousePressed(int x, int y) {
		super.mousePressed(x, y);
		suppressClick = false; // OS tidak selalu mengirim click setelah drag, jadi reset di sini
		pressX = x;
		pressY = y;
		mouseX = x;
		mouseY = y;
		dragging = false;
		dragSlot = deckSlotAt(x, y);
		PlantType type = dragSlot >= 0 ? deck.get(dragSlot) : inventoryCardAt(x, y);
		dragType = type != null && isUnlocked(type) ? type : null;
	}

	@Override
	public void mouseDragged(int x, int y) {
		mouseX = x;
		mouseY = y;
		if (!dragging && dragType != null && Math.abs(x - pressX) + Math.abs(y - pressY) > DRAG_THRESHOLD) {
			dragging = true;
			selectedSlot = -1;
		}
	}

	@Override
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void mouseReleased(int x, int y) {
		super.mouseReleased(x, y);
		if (dragging) {
			drop(x, y);
			suppressClick = true; // klik setelah drag tidak dihitung
		}
		dragging = false;
		dragSlot = -1;
		dragType = null;
	}

	private void drop(int x, int y) {
		int target = dropSlotAt(x, y);
		if (dragSlot >= 0) {
			// kartu dari deck
			if (target < 0) {
				toggle(dragType); // diseret keluar dari deck = dikeluarkan
			} else {
				int to = Math.min(target, deck.size() - 1);
				if (to != dragSlot) {
					deck.swap(dragSlot, to);
					toast.show("Posisi " + (dragSlot + 1) + " dan " + (to + 1) + " ditukar");
				}
			}
			return;
		}
		// tanaman dari inventory
		if (target < 0) {
			return;
		}
		int existing = deck.indexOf(dragType);
		if (target >= deck.size()) {
			if (existing < 0) {
				toggle(dragType); // slot kosong: tambahkan
			}
		} else if (existing >= 0) {
			if (existing != target) {
				deck.swap(existing, target);
				toast.show("Posisi " + (existing + 1) + " dan " + (target + 1) + " ditukar");
			}
		} else {
			PlantType old = deck.get(target);
			deck.replace(target, dragType);
			toast.show(old.getDisplayName() + " diganti " + dragType.getDisplayName());
		}
	}

	@Override
	public void mouseClicked(int x, int y) {
		if (suppressClick) {
			suppressClick = false;
			return;
		}
		super.mouseClicked(x, y);
		int slot = deckSlotAt(x, y);
		if (slot >= 0) {
			clickDeckSlot(slot);
			return;
		}
		PlantType type = inventoryCardAt(x, y);
		if (type != null) {
			clickInventoryCard(type);
		}
	}

	@Override
	public void rightClicked(int x, int y) {
		int slot = deckSlotAt(x, y);
		selectedSlot = -1;
		if (slot >= 0) {
			toggle(deck.get(slot));
		}
	}

	public int getSelectedSlot() {
		return selectedSlot;
	}

	public boolean isDragging() {
		return dragging;
	}

	// ------------------------------------------------------------------ render

	@Override
	protected void renderContent(Graphics2D g) {
		UI.antialias(g);
		PlantType[] types = PlantType.values();
		for (int i = 0; i < types.length; i++) {
			Rectangle r = gridCard(i);
			g.drawImage(Assets.get(types[i].getCardImage(), r.width, r.height), r.x, r.y, null);
			if (!isUnlocked(types[i])) {
				g.setColor(new Color(20, 20, 20, 200));
				g.fillRect(r.x, r.y, r.width, r.height);
				UI.centered(g, "?", r.x + r.width / 2, r.y + r.height / 2 + 10, UI.font(28), new Color(170, 170, 170));
			} else if (deck.contains(types[i])) {
				g.setColor(new Color(0, 0, 0, 130)); // sudah dipilih
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}

		int slot = 0;
		for (PlantType type : deck.getCards()) {
			Rectangle r = deckCard(slot);
			g.drawImage(Assets.get(type.getCardImage(), 66, 82), r.x + 12, r.y + 4, null);
			g.setFont(new Font("Arial", Font.BOLD, 13));
			UI.shadowText(g, Integer.toString(slot + 1), r.x + 16, r.y + 18, Color.WHITE); // tombol saat bermain
			if (dragging && slot == dragSlot) {
				g.setColor(new Color(0, 0, 0, 140));
				g.fillRect(r.x + 12, r.y + 4, 66, 82);
			}
			if (slot == selectedSlot) {
				g.setColor(new Color(255, 230, 0));
				g.setStroke(new BasicStroke(4));
				g.drawRoundRect(r.x + 10, r.y + 2, 70, 86, 10, 10);
				g.setStroke(new BasicStroke(1));
			}
			slot++;
		}

		Level level = game.getSelectedLevel();
		UI.centered(g, level.getTitle(), 370, 132, UI.font(14), new Color(255, 240, 200));
		UI.shadowText(g, "Deck " + deck.size() + "/" + requiredCards(), 20, 408, Color.WHITE);

		if (dragging) {
			drawDrag(g);
		}
		toast.draw(g, 400);
	}

	private void drawDrag(Graphics2D g) {
		int target = dropSlotAt(mouseX, mouseY);
		if (target < 0 && dragSlot >= 0) {
			// di luar deck: kartu akan dikeluarkan
			g.setColor(new Color(255, 60, 40, 60));
			g.fillRect(0, DECK_SLOT, Board.WIDTH, Board.HEIGHT - DECK_SLOT);
		}
		// kartu yang diseret (transparan, mengikuti mouse)
		Composite old = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
		g.drawImage(Assets.get(dragType.getCardImage()), mouseX - CARD_W / 2, mouseY - CARD_H / 2, CARD_W, CARD_H,
				null);
		g.setComposite(old);
		if (target >= 0) {
			// slot tujuan: border hijau (digambar paling atas supaya tidak tertutup)
			Rectangle r = deckCard(Math.min(target, dragSlot >= 0 ? deck.size() - 1 : target));
			g.setColor(new Color(80, 255, 80));
			g.setStroke(new BasicStroke(4));
			g.drawRoundRect(r.x + 10, r.y + 2, 70, 86, 10, 10);
			g.setStroke(new BasicStroke(1));
		}
	}

}
