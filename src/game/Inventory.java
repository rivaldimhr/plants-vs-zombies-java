package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Rectangle;

import entity.plant.PlantType;

/**
 * Layar pemilihan deck (maksimal 6 tanaman), lalu Start.
 * - Klik kartu inventory: masukkan ke / keluarkan dari deck
 * - Klik kartu deck: pilih untuk swap. Lalu klik kartu deck lain untuk menukar posisi,
 *   atau klik kartu inventory (yang belum ada di deck) untuk mengganti slot itu
 * - Klik kanan kartu deck: keluarkan dari deck
 * - Drag and drop: seret kartu deck ke slot lain (swap) atau keluar dari deck (hapus),
 *   seret tanaman inventory ke slot deck (ganti / isi slot kosong)
 */
public class Inventory extends BaseScreen {
	// Posisi kartu di grid inventory (2 baris x 5 kolom)
	private static final int GRID_X = 90, GRID_Y = 160, GRID_DX = 85, GRID_DY = 95;
	private static final int CARD_W = 80, CARD_H = 90;
	// Posisi kartu di deck (baris atas)
	private static final int DECK_SLOT = 90;

	private final Deck deck;
	private final Toast toast = new Toast();
	private volatile int selectedSlot = -1; // kartu deck yang dipilih untuk swap

	// Drag and drop
	private static final int DRAG_THRESHOLD = 6; // pixel sebelum dianggap drag, bukan klik
	private int pressX, pressY;
	private int dragSlot = -1; // sumber drag dari deck
	private PlantType dragType = null; // tanaman yang sedang diseret
	private boolean dragging = false;
	private boolean suppressClick = false;
	private int mouseX, mouseY;

	public Inventory(Game game) {
		super(game);
		this.deck = game.getDeck();
		addButton("Start", 570, 10, 80, 30, this::start);
		addButton("Clear Deck", 570, 50, 80, 30, () -> {
			deck.clear();
			selectedSlot = -1;
			toast.show("Deck dikosongkan");
		});
	}

	@Override
	protected String getBackground() {
		return "image/IMAGE/INVENTORY (7).png";
	}

	private void start() {
		if (!deck.isFull()) {
			toast.show("Deck belum lengkap, pilih " + Deck.SIZE + " tanaman! (" + deck.size() + "/" + Deck.SIZE + ")");
			return;
		}
		toast.clear();
		selectedSlot = -1;
		game.startNewGame();
	}

	private static Rectangle gridCard(int index) {
		int col = index % 5;
		int row = index / 5;
		return new Rectangle(GRID_X + GRID_DX * col, GRID_Y + GRID_DY * row, CARD_W, CARD_H);
	}

	private static Rectangle deckCard(int slot) {
		return new Rectangle(slot * DECK_SLOT, 0, DECK_SLOT, DECK_SLOT);
	}

	// Slot deck di posisi (x, y), atau -1
	private int deckSlotAt(int x, int y) {
		for (int slot = 0; slot < deck.size(); slot++) {
			if (deckCard(slot).contains(x, y)) {
				return slot;
			}
		}
		return -1;
	}

	private void toggle(PlantType type) {
		boolean wasInDeck = deck.contains(type);
		if (!deck.toggle(type)) {
			toast.show("Deck sudah penuh. Klik kartu deck lalu kartu ini untuk mengganti");
		} else if (wasInDeck) {
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

	private PlantType inventoryCardAt(int x, int y) {
		PlantType[] types = PlantType.values();
		for (int i = 0; i < types.length; i++) {
			if (gridCard(i).contains(x, y)) {
				return types[i];
			}
		}
		return null;
	}

	// Slot deck tujuan drop (termasuk slot kosong di baris deck), atau -1
	private static int dropSlotAt(int x, int y) {
		if (y >= 0 && y < DECK_SLOT && x >= 0 && x < Deck.SIZE * DECK_SLOT) {
			return x / DECK_SLOT;
		}
		return -1;
	}

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
		dragType = dragSlot >= 0 ? deck.get(dragSlot) : inventoryCardAt(x, y);
	}

	@Override
	public void mouseDragged(int x, int y) {
		mouseX = x;
		mouseY = y;
		if (!dragging && dragType != null
				&& Math.abs(x - pressX) + Math.abs(y - pressY) > DRAG_THRESHOLD) {
			dragging = true;
			selectedSlot = -1;
		}
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
			} else if (target != dragSlot) {
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

	public boolean isDragging() {
		return dragging;
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
		PlantType[] types = PlantType.values();
		for (int i = 0; i < types.length; i++) {
			if (gridCard(i).contains(x, y)) {
				clickInventoryCard(types[i]);
				return;
			}
		}
	}

	@Override
	public void rightClicked(int x, int y) {
		int slot = deckSlotAt(x, y);
		if (slot >= 0) {
			selectedSlot = -1;
			toggle(deck.get(slot));
		} else {
			selectedSlot = -1;
		}
	}

	public int getSelectedSlot() {
		return selectedSlot;
	}

	@Override
	protected void renderContent(Graphics2D g) {
		PlantType[] types = PlantType.values();
		for (int i = 0; i < types.length; i++) {
			Rectangle r = gridCard(i);
			g.drawImage(Assets.get(types[i].getCardImage()), r.x, r.y, r.width, r.height, null);
			if (deck.contains(types[i])) {
				// kartu yang sudah dipilih digelapkan
				g.setColor(new Color(0, 0, 0, 130));
				g.fillRect(r.x, r.y, r.width, r.height);
			}
		}

		int slot = 0;
		for (PlantType type : deck.getCards()) {
			Rectangle r = deckCard(slot);
			g.drawImage(Assets.get(type.getCardImage()), r.x, r.y, r.width, r.height, null);
			g.setFont(new Font("Arial", Font.BOLD, 13));
			g.setColor(Color.BLACK);
			g.drawString(Integer.toString(slot + 1), r.x + 7, r.y + 16); // tombol saat bermain
			g.setColor(Color.WHITE);
			g.drawString(Integer.toString(slot + 1), r.x + 6, r.y + 15);
			if (dragging && slot == dragSlot) {
				g.setColor(new Color(0, 0, 0, 140));
				g.fillRect(r.x, r.y, r.width, r.height);
			}
			if (slot == selectedSlot) {
				g.setColor(new Color(255, 230, 0));
				g.setStroke(new BasicStroke(4));
				g.drawRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 10, 10);
				g.setStroke(new BasicStroke(1));
			}
			slot++;
		}

		if (dragging) {
			drawDrag(g);
		}
		toast.draw(g, 395);
	}

	private void drawDrag(Graphics2D g) {
		int target = dropSlotAt(mouseX, mouseY);
		if (target < 0 && dragSlot >= 0) {
			// di luar deck: kartu akan dikeluarkan
			g.setColor(new Color(255, 60, 40, 60));
			g.fillRect(0, DECK_SLOT, Board.WIDTH, Board.HEIGHT - DECK_SLOT);
		}
		// kartu yang diseret (sedikit lebih kecil & transparan)
		int w = CARD_W * 3 / 4, h = CARD_H * 3 / 4;
		Composite old = g.getComposite();
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
		g.drawImage(Assets.get(dragType.getCardImage()), mouseX - w / 2, mouseY - h / 2, w, h, null);
		g.setComposite(old);
		if (target >= 0) {
			// slot tujuan: border hijau (digambar paling atas supaya tidak tertutup)
			Rectangle r = deckCard(Math.min(target, dragSlot >= 0 ? deck.size() - 1 : target));
			g.setColor(new Color(80, 255, 80));
			g.setStroke(new BasicStroke(4));
			g.drawRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 10, 10);
			g.setStroke(new BasicStroke(1));
		}
	}


}
