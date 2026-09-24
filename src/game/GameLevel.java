package game;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import entity.plant.LilyPad;
import entity.plant.Plant;
import entity.plant.PlantType;
import entity.projectile.Bullet;
import entity.zombie.Zombie;

/**
 * Layar permainan: menerima input (mouse & keyboard), menggambar papan + HUD,
 * dan menjalankan Board tiap tick.
 *
 * Mouse : klik kartu lalu klik tile untuk menanam, klik sekop lalu klik tanaman
 *         untuk mencabut, klik kanan untuk batal.
 * Keyboard: panah = cursor, Enter = pilih tile, 1-6 = tanam di tile terpilih,
 *         7 = sekop di tile terpilih, P / Esc = pause.
 */
public class GameLevel implements ScreenMethod {
    private static final int TILE = Board.TILE_SIZE;
    private static final int SHOVEL_KEY = 7;
    private static final int HUGE_WAVE_TEXT_SECONDS = 4;
    private static final int LILY_PAD_LIFT = 12; // tanaman di atas Lily Pad digambar sedikit lebih tinggi

    // Layout HUD (baris paling atas)
    private static final int CARD_X = 60, CARD_Y = 4, CARD_W = 42, CARD_H = 52, CARD_GAP = 48;
    private static final Rectangle SHOVEL_SLOT = new Rectangle(CARD_X + Deck.SIZE * CARD_GAP + 4, 4, 52, 52);
    private static final Rectangle TIME_PANEL = new Rectangle(420, 4, 172, 52);

    private final Game game;
    private final KeyHandler keyH;
    private final Deck deck;
    private final Toast toast = new Toast();
    private final Queue<Point> clicks = new ConcurrentLinkedQueue<>();
    private final MyButton pauseButton;
    private final MyButton resumeButton;
    private final MyButton menuButton;
    private final Image backgroundDay = Assets.get("image/IMAGE/BACKGROUND DAY.png");
    private final Image backgroundNight = Assets.get("image/IMAGE/BACKGROUND NIGHT.png");

    private volatile Board board = new Board(new Random());
    private volatile boolean paused = false;
    private volatile int cursorCol = 1, cursorRow = 1; // cursor keyboard
    private volatile int selectedCol = -1, selectedRow = -1; // tile terpilih (Enter / klik)
    private volatile int hoverCol = -1, hoverRow = -1; // tile di bawah mouse
    private volatile int selectedCard = -1; // kartu yang dipilih dengan mouse
    private volatile boolean shovelMode = false;

    public GameLevel(Game game) {
        this.game = game;
        this.keyH = game.getKeyHandler();
        this.deck = game.getDeck();
        // aksi tombol dijalankan di thread game (lihat handleClick)
        pauseButton = new MyButton("Pause", 598, 16, 56, 28, () -> paused = true);
        resumeButton = new MyButton("Lanjut", 255, 200, 150, 36, () -> paused = false);
        menuButton = new MyButton("Menu Utama", 255, 246, 150, 36, () -> game.setStates(States.MENU));
    }

    // Mulai permainan baru
    public void reset() {
        board = new Board(new Random());
        deck.resetCooldowns();
        keyH.reset();
        clicks.clear();
        toast.clear();
        paused = false;
        cursorCol = 1;
        cursorRow = 1;
        selectedCol = -1;
        selectedRow = -1;
        selectedCard = -1;
        shovelMode = false;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isPaused() {
        return paused;
    }

    public int getSelectedCard() {
        return selectedCard;
    }

    public boolean isShovelMode() {
        return shovelMode;
    }

    public String getToastMessage() {
        return toast.getMessage();
    }

    // ================================================================== update (thread game)

    @Override
    public void update() {
        handleKeys();
        Point click;
        while ((click = clicks.poll()) != null) {
            handleClick(click.x, click.y);
        }
        if (paused || game.getStates() != States.GAME_LEVEL) {
            return;
        }

        board.update();
        deck.tick();

        switch (board.getResult()) {
            case WON:
                game.setStates(States.WIN);
                break;
            case LOST:
                game.setStates(States.GAME_OVER);
                break;
            default:
                break;
        }
    }

    private void handleKeys() {
        if (keyH.pausePressed) {
            keyH.pausePressed = false;
            paused = !paused;
        }
        if (paused) {
            keyH.reset();
            return;
        }
        // cursor bergerak di halaman (baris 1-6), wrap di tepi
        if (keyH.upPressed) {
            keyH.upPressed = false;
            cursorRow = cursorRow <= 1 ? Board.ROWS - 1 : cursorRow - 1;
        }
        if (keyH.downPressed) {
            keyH.downPressed = false;
            cursorRow = cursorRow >= Board.ROWS - 1 ? 1 : cursorRow + 1;
        }
        if (keyH.leftPressed) {
            keyH.leftPressed = false;
            cursorCol = cursorCol <= 0 ? Board.COLS - 1 : cursorCol - 1;
        }
        if (keyH.rightPressed) {
            keyH.rightPressed = false;
            cursorCol = cursorCol >= Board.COLS - 1 ? 0 : cursorCol + 1;
        }
        if (keyH.enterPressed) {
            keyH.enterPressed = false;
            selectedCol = cursorCol;
            selectedRow = cursorRow;
        }
        if (keyH.numPressed) {
            int key = keyH.numkey;
            keyH.numPressed = false; // input selalu dipakai, walaupun gagal menanam
            if (selectedCol < 0) {
                toast.show("Pilih tile dulu (Enter atau klik)");
            } else if (key == SHOVEL_KEY) {
                dig(selectedCol, selectedRow);
            } else {
                plantFromDeck(key - 1, selectedCol, selectedRow);
            }
        }
    }

    private void handleClick(int x, int y) {
        if (paused) {
            if (resumeButton.contains(x, y)) {
                resumeButton.click();
            } else if (menuButton.contains(x, y)) {
                menuButton.click();
            }
            return;
        }
        if (pauseButton.contains(x, y)) {
            pauseButton.click();
            return;
        }
        for (int slot = 0; slot < deck.size(); slot++) {
            if (cardBounds(slot).contains(x, y)) {
                selectCard(slot);
                return;
            }
        }
        if (SHOVEL_SLOT.contains(x, y)) {
            shovelMode = !shovelMode;
            selectedCard = -1;
            return;
        }

        int col = x / TILE;
        int row = y / TILE;
        if (row >= 1 && row < Board.ROWS && col >= 0 && col < Board.COLS) {
            cursorCol = col;
            cursorRow = row;
            selectedCol = col;
            selectedRow = row;
            if (shovelMode) {
                dig(col, row);
                shovelMode = false;
            } else if (selectedCard >= 0 && plantFromDeck(selectedCard, col, row)) {
                selectedCard = -1;
            }
            return;
        }
        cancelSelection();
    }

    private void selectCard(int slot) {
        shovelMode = false;
        if (selectedCard == slot) {
            selectedCard = -1;
            return;
        }
        String problem = cardProblem(slot);
        if (problem != null) {
            toast.show(problem);
            return;
        }
        selectedCard = slot;
    }

    private void cancelSelection() {
        selectedCard = -1;
        shovelMode = false;
    }

    // Alasan kartu belum bisa dipakai, atau null kalau siap
    private String cardProblem(int slot) {
        PlantType type = deck.get(slot);
        if (type == null) {
            return "Slot kosong";
        }
        if (!deck.isReady(slot)) {
            return type.getDisplayName() + " masih cooldown (" + deck.getCooldownSeconds(slot) + " detik)";
        }
        if (board.getSun().get() < type.getCost()) {
            return "Sun tidak cukup untuk " + type.getDisplayName() + " (butuh " + type.getCost() + ")";
        }
        return null;
    }

    // Tanam kartu di slot pada tile (col, row). Return true kalau berhasil.
    private boolean plantFromDeck(int slot, int col, int row) {
        PlantType type = deck.get(slot);
        if (type == null) {
            return false;
        }
        String problem = cardProblem(slot);
        if (problem != null) {
            toast.show(problem);
            return false;
        }
        if (!board.plant(type, col, row)) {
            toast.show(type.getDisplayName() + " tidak bisa ditanam di sini");
            return false;
        }
        deck.startCooldown(slot);
        return true;
    }

    private void dig(int col, int row) {
        if (!board.dig(col, row)) {
            toast.show("Tidak ada tanaman untuk dicabut");
        }
    }

    // ================================================================== input (thread UI)

    @Override
    public void mousePressed(int x, int y) {
        clicks.add(new Point(x, y));
    }

    @Override
    public void mouseClicked(int x, int y) {
    }

    @Override
    public void mouseReleased(int x, int y) {
    }

    @Override
    public void mouseMoved(int x, int y) {
        int row = y / TILE;
        hoverCol = row >= 1 ? x / TILE : -1;
        hoverRow = row >= 1 ? row : -1;
        pauseButton.setMouseOver(pauseButton.contains(x, y));
        resumeButton.setMouseOver(resumeButton.contains(x, y));
        menuButton.setMouseOver(menuButton.contains(x, y));
    }

    @Override
    public void rightClicked(int x, int y) {
        cancelSelection();
    }

    // ================================================================== render (thread UI)

    private static Rectangle cardBounds(int slot) {
        return new Rectangle(CARD_X + slot * CARD_GAP, CARD_Y, CARD_W, CARD_H);
    }

    @Override
    public void render(Graphics2D g) {
        Board b = board;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawBackground(g, b);
        drawTileHighlights(g, b);

        for (Plant plant : b.getPlants()) {
            int lift = liftOf(b, plant);
            g.translate(0, -lift);
            plant.draw(g);
            g.translate(0, lift);
        }
        for (Zombie zombie : b.getZombies()) {
            zombie.draw(g);
        }
        for (Bullet bullet : b.getBullets()) {
            bullet.draw(g);
        }
        for (Plant plant : b.getPlants()) {
            int lift = liftOf(b, plant);
            g.translate(0, -lift);
            plant.drawHealthBar(g);
            g.translate(0, lift);
        }
        for (Zombie zombie : b.getZombies()) {
            zombie.drawHealthBar(g);
        }

        drawPlantPreview(g, b);
        g.drawImage(Assets.get("image/cursor.png"), cursorCol * TILE, cursorRow * TILE, TILE, TILE, null);

        drawHud(g, b);
        drawHugeWaveText(g, b);
        toast.draw(g, 395);
        if (paused) {
            drawPauseOverlay(g);
        }
    }

    private static int liftOf(Board b, Plant plant) {
        if (plant instanceof LilyPad) {
            return 0;
        }
        for (Plant other : b.getPlantsAt(plant.getCol(), plant.getRow())) {
            if (other instanceof LilyPad) {
                return LILY_PAD_LIFT;
            }
        }
        return 0;
    }

    // Siang dan malam bergantian dengan transisi fade 20 detik
    private void drawBackground(Graphics2D g, Board b) {
        if (backgroundDay == null || backgroundNight == null) {
            g.setColor(Color.GRAY);
            g.fillRect(0, 0, Board.WIDTH, Board.HEIGHT);
            return;
        }
        double t = ((double) b.getTicks() / Game.UPS) % Board.DAY_NIGHT_CYCLE;
        if (t < 80) {
            g.drawImage(backgroundDay, 0, 0, Board.WIDTH, Board.HEIGHT, null);
        } else if (t < 100) {
            drawFade(g, backgroundDay, backgroundNight, (float) (t - 80) / 20f);
        } else if (t < 180) {
            g.drawImage(backgroundNight, 0, 0, Board.WIDTH, Board.HEIGHT, null);
        } else {
            drawFade(g, backgroundNight, backgroundDay, (float) (t - 180) / 20f);
        }
    }

    private void drawFade(Graphics2D g, Image from, Image to, float alpha) {
        g.drawImage(from, 0, 0, Board.WIDTH, Board.HEIGHT, null);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, Math.max(0f, alpha))));
        g.drawImage(to, 0, 0, Board.WIDTH, Board.HEIGHT, null);
        g.setComposite(old);
    }

    private void drawTileHighlights(Graphics2D g, Board b) {
        if (selectedCol >= 0) {
            g.setColor(new Color(255, 230, 0, 60));
            g.fillRect(selectedCol * TILE, selectedRow * TILE, TILE, TILE);
            g.setColor(new Color(255, 230, 0));
            g.setStroke(new BasicStroke(2));
            g.drawRect(selectedCol * TILE + 1, selectedRow * TILE + 1, TILE - 2, TILE - 2);
            g.setStroke(new BasicStroke(1));
        }
        if (shovelMode && hoverCol >= 0 && !b.getPlantsAt(hoverCol, hoverRow).isEmpty()) {
            g.setColor(new Color(255, 60, 40, 90));
            g.fillRect(hoverCol * TILE, hoverRow * TILE, TILE, TILE);
        }
    }

    // Bayangan tanaman di tile yang disorot mouse: hijau = bisa ditanam, merah = tidak
    private void drawPlantPreview(Graphics2D g, Board b) {
        int slot = selectedCard;
        PlantType type = deck.get(slot);
        if (type == null || hoverCol < 0) {
            return;
        }
        boolean ok = b.canPlant(type, hoverCol, hoverRow) && b.getSun().get() >= type.getCost();
        g.setColor(ok ? new Color(80, 255, 80, 70) : new Color(255, 60, 40, 80));
        g.fillRect(hoverCol * TILE, hoverRow * TILE, TILE, TILE);
        type.create(hoverCol * TILE, hoverRow * TILE).drawPreview(g, 0.55f);
    }

    private void drawHud(Graphics2D g, Board b) {
        // panel seed bank
        g.setColor(new Color(70, 40, 15, 215));
        g.fillRoundRect(2, 2, SHOVEL_SLOT.x + SHOVEL_SLOT.width + 4, 56, 12, 12);

        // sun
        g.drawImage(Assets.get("image/sprites/sun.gif"), 12, 2, 36, 36, Assets.observer());
        drawCenteredText(g, Integer.toString(b.getSun().get()), 30, 53, new Font("Arial", Font.BOLD, 14),
                Color.WHITE);

        // kartu deck
        for (int slot = 0; slot < deck.size(); slot++) {
            drawCard(g, b, slot);
        }

        // sekop
        g.setColor(new Color(40, 25, 10, 200));
        g.fillRoundRect(SHOVEL_SLOT.x, SHOVEL_SLOT.y, SHOVEL_SLOT.width, SHOVEL_SLOT.height, 10, 10);
        g.drawImage(Assets.get("image/sprites/shovel.png"), SHOVEL_SLOT.x + 7, SHOVEL_SLOT.y + 5, 38, 41, null);
        if (shovelMode) {
            drawSelectionBorder(g, SHOVEL_SLOT);
        }
        drawKeyLabel(g, "7", SHOVEL_SLOT.x + 3, SHOVEL_SLOT.y + 12);

        drawTimePanel(g, b);
        pauseButton.draw(g);
    }

    private void drawCard(Graphics2D g, Board b, int slot) {
        PlantType type = deck.get(slot);
        Rectangle r = cardBounds(slot);
        g.drawImage(Assets.get(type.getCardImage()), r.x, r.y, r.width, r.height, null);

        if (!deck.isReady(slot)) {
            // overlay gelap yang menyusut dari atas sesuai sisa cooldown
            int h = (int) Math.ceil(r.height * deck.getCooldownFraction(slot));
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(r.x, r.y, r.width, h);
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRect(r.x, r.y, r.width, r.height);
            drawCenteredText(g, Integer.toString(deck.getCooldownSeconds(slot)), r.x + r.width / 2,
                    r.y + r.height / 2 + 6, new Font("Arial", Font.BOLD, 16), Color.WHITE);
        } else if (b.getSun().get() < type.getCost()) {
            g.setColor(new Color(40, 40, 40, 120));
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        if (selectedCard == slot) {
            drawSelectionBorder(g, r);
        }
        drawKeyLabel(g, Integer.toString(slot + 1), r.x + 3, r.y + 12);
    }

    private void drawSelectionBorder(Graphics2D g, Rectangle r) {
        g.setColor(new Color(255, 230, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2, 8, 8);
        g.setStroke(new BasicStroke(1));
    }

    private void drawKeyLabel(Graphics2D g, String text, int x, int y) {
        g.setFont(new Font("Arial", Font.BOLD, 11));
        g.setColor(new Color(0, 0, 0, 170));
        g.drawString(text, x + 1, y + 1);
        g.setColor(Color.WHITE);
        g.drawString(text, x, y);
    }

    // Waktu, progress, dan siang/malam
    private void drawTimePanel(Graphics2D g, Board b) {
        Rectangle r = TIME_PANEL;
        g.setColor(new Color(70, 40, 15, 215));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 12, 12);

        int time = b.getTime();
        g.setFont(new Font("Arial", Font.BOLD, 13));
        g.setColor(Color.WHITE);
        g.drawString("Waktu " + Math.min(time, Board.GAME_DURATION) + " / " + Board.GAME_DURATION, r.x + 10, r.y + 17);
        String phase = b.isHugeWave() ? "Huge Wave!" : b.isDay() ? "Siang" : "Malam";
        g.setColor(b.isHugeWave() ? new Color(255, 90, 70) : b.isDay() ? new Color(255, 220, 90)
                : new Color(160, 180, 255));
        g.drawString(phase, r.x + 10, r.y + 33);

        int barX = r.x + 10, barY = r.y + 40, barW = r.width - 20, barH = 7;
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRoundRect(barX, barY, barW, barH, 6, 6);
        g.setColor(new Color(120, 200, 70));
        int fill = (int) (barW * Math.min(1.0, (double) time / Board.GAME_DURATION));
        g.fillRoundRect(barX, barY, fill, barH, 6, 6);
        // penanda mulai huge wave
        int waveX = barX + barW * ZombieSpawner.HUGE_WAVE_START / Board.GAME_DURATION;
        g.setColor(new Color(255, 90, 70));
        g.fillRect(waveX, barY - 2, 2, barH + 4);
    }

    private void drawHugeWaveText(Graphics2D g, Board b) {
        int time = b.getTime();
        if (time < ZombieSpawner.HUGE_WAVE_START || time >= ZombieSpawner.HUGE_WAVE_START + HUGE_WAVE_TEXT_SECONDS) {
            return;
        }
        if ((b.getTicks() / 20) % 2 == 0) { // berkedip
            int w = 560, h = 30;
            g.drawImage(Assets.get("image/huge_wave_of_zombies_text.png"), (Board.WIDTH - w) / 2, 190, w, h, null);
        }
    }

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, Board.WIDTH, Board.HEIGHT);
        drawCenteredText(g, "PAUSE", Board.WIDTH / 2, 170, new Font("Arial", Font.BOLD, 42), Color.WHITE);
        resumeButton.draw(g);
        menuButton.draw(g);
        drawCenteredText(g, "Tekan P / Esc untuk lanjut", Board.WIDTH / 2, 312, new Font("Arial", Font.PLAIN, 13),
                new Color(220, 220, 220));
    }

    private void drawCenteredText(Graphics2D g, String text, int centerX, int baselineY, Font font, Color color) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(text, x + 1, baselineY + 1);
        g.setColor(color);
        g.drawString(text, x, baselineY);
    }
}
