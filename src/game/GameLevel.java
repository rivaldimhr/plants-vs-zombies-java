package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import entity.LawnMower;
import entity.SunToken;
import entity.effect.Effect;
import entity.plant.LilyPad;
import entity.plant.Plant;
import entity.plant.PlantType;
import entity.projectile.Bullet;
import entity.zombie.Zombie;

/**
 * Layar permainan: menerima input (mouse & keyboard), menggambar papan + HUD,
 * dan menjalankan Board tiap tick.
 *
 * Mouse : klik sun untuk mengambil, klik kartu lalu klik tile untuk menanam,
 *         klik sekop lalu klik tanaman untuk mencabut, klik kanan untuk batal.
 * Keyboard: panah = cursor, Enter = pilih tile, 1-6 = tanam di tile terpilih,
 *         7 = sekop di tile terpilih, F = kecepatan 2x, P / Esc = pause.
 */
public class GameLevel implements ScreenMethod {
    private static final int TILE = Board.TILE_SIZE;
    private static final int SHOVEL_KEY = 7;
    private static final int LILY_PAD_LIFT = 12; // tanaman di atas Lily Pad digambar sedikit lebih tinggi
    private static final int INTRO_TICKS = (int) (2.4 * Game.UPS);
    private static final int END_TICKS = (int) (2.2 * Game.UPS);

    // Seed bank (image/ui/deck.png 1166x204) diperkecil jadi tinggi 66 px
    private static final double BANK_SCALE = 66 / 204.0;
    private static final int BANK_W = (int) Math.round(1166 * BANK_SCALE);
    private static final int BANK_H = 66;
    private static final int[] SLOT_X = { 203, 333, 463, 597, 737, 870 }; // koordinat asli deck.png
    private static final int SLOT_Y = 37, SLOT_W = 93, SLOT_H = 139;
    private static final Rectangle SHOVEL_SLOT = scaled(1000, 8, 160, 188);
    private static final Rectangle PROGRESS = new Rectangle(490, 402, 150, 12);

    private final Game game;
    private final KeyHandler keyH;
    private final Deck deck;
    private final Toast toast = new Toast();
    private final Queue<Point> clicks = new ConcurrentLinkedQueue<>();
    private final MyButton pauseButton;
    private final MyButton speedButton;
    private final MyButton resumeButton;
    private final MyButton restartButton;
    private final MyButton menuButton;
    private final MyButton soundButton;
    private final MyButton musicButton;
    private final MyButton[] pauseMenu;

    private volatile Board board = new Board(new Random());
    private volatile boolean paused = false;
    private volatile boolean fast = false;
    private volatile int introTicks = 0;
    private volatile int endTicks = 0;
    private volatile int cursorCol = 1, cursorRow = 1; // cursor keyboard
    private volatile int selectedCol = -1, selectedRow = -1; // tile terpilih (Enter / klik)
    private volatile int hoverCol = -1, hoverRow = -1; // tile di bawah mouse
    private volatile int mouseX = -100, mouseY = -100;
    private volatile int selectedCard = -1; // kartu yang dipilih dengan mouse
    private volatile boolean shovelMode = false;

    public GameLevel(Game game) {
        this.game = game;
        this.keyH = game.getKeyHandler();
        this.deck = game.getDeck();
        // aksi tombol dijalankan di thread game (lihat handleClick)
        pauseButton = new MyButton("Pause", 600, 6, 54, 24, () -> paused = true);
        speedButton = new MyButton("1x", 540, 6, 54, 24, this::toggleSpeed);
        resumeButton = new MyButton("Lanjut", 255, 150, 150, 34, () -> paused = false);
        restartButton = new MyButton("Ulangi Level", 255, 192, 150, 34, () -> start(board.getLevel()));
        menuButton = new MyButton("Menu Utama", 255, 234, 150, 34, () -> game.setStates(States.MENU));
        soundButton = new MyButton("", 255, 284, 72, 28, this::toggleSound);
        musicButton = new MyButton("", 333, 284, 72, 28, this::toggleMusic);
        pauseMenu = new MyButton[] { resumeButton, restartButton, menuButton, soundButton, musicButton };
    }

    private static Rectangle scaled(int x, int y, int w, int h) {
        return new Rectangle((int) Math.round(x * BANK_SCALE), (int) Math.round(y * BANK_SCALE),
                (int) Math.round(w * BANK_SCALE), (int) Math.round(h * BANK_SCALE));
    }

    private static Rectangle cardBounds(int slot) {
        return scaled(SLOT_X[slot], SLOT_Y, SLOT_W, SLOT_H);
    }

    // Mulai (atau ulangi) level
    public void start(Level level) {
        board = new Board(level, new Random());
        board.addListener(game.getSound());
        deck.resetCooldowns();
        keyH.reset();
        clicks.clear();
        toast.clear();
        paused = false;
        introTicks = INTRO_TICKS;
        endTicks = 0;
        cursorCol = 1;
        cursorRow = 1;
        selectedCol = -1;
        selectedRow = -1;
        selectedCard = -1;
        shovelMode = false;
        game.getSound().playMusic(level.isNight() ? "music_night" : "music_day");
    }

    @Override
    public void onShow() {
        game.getSound().playMusic(board.getLevel().isNight() ? "music_night" : "music_day");
    }

    // ------------------------------------------------------------------ getter (dipakai test & layar lain)

    public Board getBoard() {
        return board;
    }

    public boolean isPaused() {
        return paused;
    }

    public boolean isFast() {
        return fast;
    }

    public boolean isIntroPlaying() {
        return introTicks > 0;
    }

    public void skipIntro() {
        introTicks = 0;
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
        if (introTicks > 0) {
            introTicks--;
            return;
        }
        if (board.getResult() != Board.Result.PLAYING) {
            // jeda singkat untuk menampilkan tulisan akhir sebelum pindah layar
            if (++endTicks >= END_TICKS) {
                game.finishLevel(board);
            }
            return;
        }
        int steps = fast ? 2 : 1;
        for (int i = 0; i < steps && board.getResult() == Board.Result.PLAYING; i++) {
            board.update();
            deck.tick();
        }
    }

    private void toggleSpeed() {
        fast = !fast;
        speedButton.setText(fast ? "2x" : "1x");
    }

    private void toggleSound() {
        SaveData save = game.getSaveData();
        save.setSoundOn(!save.isSoundOn());
    }

    private void toggleMusic() {
        SaveData save = game.getSaveData();
        save.setMusicOn(!save.isMusicOn());
        game.getSound().refreshMusic();
    }

    private void handleKeys() {
        if (keyH.pausePressed) {
            keyH.pausePressed = false;
            if (board.getResult() == Board.Result.PLAYING) {
                paused = !paused;
            }
        }
        if (paused) {
            keyH.reset();
            return;
        }
        if (keyH.speedPressed) {
            keyH.speedPressed = false;
            toggleSpeed();
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
            if (!canAct()) {
                return;
            }
            if (selectedCol < 0) {
                toast.show("Pilih tile dulu (Enter atau klik)");
            } else if (key == SHOVEL_KEY) {
                dig(selectedCol, selectedRow);
            } else {
                plantFromDeck(key - 1, selectedCol, selectedRow);
            }
        }
    }

    // Menanam hanya bisa setelah intro dan sebelum permainan selesai
    private boolean canAct() {
        return introTicks <= 0 && board.getResult() == Board.Result.PLAYING;
    }

    private void handleClick(int x, int y) {
        if (paused) {
            for (MyButton button : pauseMenu) {
                if (button.contains(x, y)) {
                    game.getSound().play("click");
                    button.click();
                    return;
                }
            }
            return;
        }
        if (pauseButton.contains(x, y) || speedButton.contains(x, y)) {
            game.getSound().play("click");
            (pauseButton.contains(x, y) ? pauseButton : speedButton).click();
            return;
        }
        if (!canAct()) {
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
        // sun didahulukan daripada menanam
        if (board.collectSunAt(x, y)) {
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
        mouseX = x;
        mouseY = y;
        int row = y / TILE;
        hoverCol = row >= 1 ? x / TILE : -1;
        hoverRow = row >= 1 ? row : -1;
        pauseButton.setMouseOver(pauseButton.contains(x, y));
        speedButton.setMouseOver(speedButton.contains(x, y));
        for (MyButton button : pauseMenu) {
            button.setMouseOver(button.contains(x, y));
        }
    }

    @Override
    public void rightClicked(int x, int y) {
        cancelSelection();
    }

    // ================================================================== render (thread UI)

    @Override
    public void render(Graphics2D g) {
        Board b = board;
        UI.antialias(g);
        g.drawImage(Assets.get(b.getLevel().isNight() ? "image/IMAGE/BACKGROUND NIGHT.png"
                : "image/IMAGE/BACKGROUND DAY.png"), 0, 0, Board.WIDTH, Board.HEIGHT, null);
        drawTileHighlights(g, b);

        for (LawnMower mower : b.getMowers()) {
            mower.draw(g);
        }
        // per baris dari atas ke bawah: zombie baris bawah menutupi baris atas
        List<Plant> plants = b.getPlants();
        List<Zombie> zombies = b.getZombies();
        for (int row = 1; row < Board.ROWS; row++) {
            for (Plant plant : plants) {
                if (plant.getRow() == row) {
                    int lift = liftOf(b, plant);
                    g.translate(0, -lift);
                    plant.draw(g);
                    g.translate(0, lift);
                }
            }
            for (Zombie zombie : zombies) {
                if (zombie.getRow() == row) {
                    zombie.draw(g);
                }
            }
        }
        for (Effect effect : b.getEffects()) {
            effect.draw(g);
        }
        for (Bullet bullet : b.getBullets()) {
            bullet.draw(g);
        }
        for (Plant plant : plants) {
            int lift = liftOf(b, plant);
            g.translate(0, -lift);
            plant.drawHealthBar(g);
            g.translate(0, lift);
        }
        for (Zombie zombie : zombies) {
            zombie.drawHealthBar(g);
        }

        drawPlantPreview(g, b);
        g.drawImage(Assets.get("image/ui/cursor.png"), cursorCol * TILE, cursorRow * TILE, TILE, TILE, null);
        for (SunToken token : b.getSuns()) {
            token.draw(g);
        }

        drawHud(g, b);
        drawBanners(g, b);
        drawHeldItem(g);
        toast.draw(g, 372);
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
        PlantType type = deck.get(selectedCard);
        if (type == null || hoverCol < 0) {
            return;
        }
        boolean ok = b.canPlant(type, hoverCol, hoverRow) && b.getSun().get() >= type.getCost();
        g.setColor(ok ? new Color(80, 255, 80, 60) : new Color(255, 60, 40, 70));
        g.fillRect(hoverCol * TILE, hoverRow * TILE, TILE, TILE);
        if (ok) {
            type.create(hoverCol * TILE, hoverRow * TILE).drawPreview(g, 0.45f);
        }
    }

    // Tanaman / sekop yang "dipegang" mengikuti mouse
    private void drawHeldItem(Graphics2D g) {
        if (paused || mouseX < 0) {
            return;
        }
        PlantType type = deck.get(selectedCard);
        if (type != null) {
            type.create(mouseX - TILE / 2, mouseY - TILE + 18).drawPreview(g, 0.85f);
        } else if (shovelMode) {
            g.drawImage(Assets.get("image/ui/shovel.png", 36, 38), mouseX - 12, mouseY - 34, null);
        }
    }

    private void drawHud(Graphics2D g, Board b) {
        g.drawImage(Assets.get("image/ui/deck.png", BANK_W, BANK_H), 0, 0, null);
        UI.centered(g, Integer.toString(b.getSun().get()), 28, 60, UI.font(12), new Color(60, 40, 10));

        for (int slot = 0; slot < deck.size(); slot++) {
            drawCard(g, b, slot);
        }
        if (!shovelMode) {
            g.drawImage(Assets.get("image/ui/shovel.png", 34, 37), SHOVEL_SLOT.x + 9, SHOVEL_SLOT.y + 10, null);
        } else {
            drawSelectionBorder(g, SHOVEL_SLOT);
        }
        drawKeyLabel(g, "7", SHOVEL_SLOT.x + 6, SHOVEL_SLOT.y + 14);

        pauseButton.draw(g);
        speedButton.draw(g);
        drawProgress(g, b);
    }

    private void drawCard(Graphics2D g, Board b, int slot) {
        PlantType type = deck.get(slot);
        Rectangle r = cardBounds(slot);
        g.drawImage(Assets.get(type.getCardImage(), r.width, r.height), r.x, r.y, null);
        if (!deck.isReady(slot)) {
            // overlay gelap yang menyusut dari atas sesuai sisa cooldown
            int h = (int) Math.ceil(r.height * deck.getCooldownFraction(slot));
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(r.x, r.y, r.width, h);
            g.setColor(new Color(0, 0, 0, 60));
            g.fillRect(r.x, r.y, r.width, r.height);
            UI.centered(g, Integer.toString(deck.getCooldownSeconds(slot)), r.x + r.width / 2, r.y + r.height / 2 + 5,
                    UI.font(12), Color.WHITE);
        } else if (b.getSun().get() < type.getCost()) {
            g.setColor(new Color(40, 40, 40, 130));
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        if (selectedCard == slot) {
            drawSelectionBorder(g, r);
        }
        drawKeyLabel(g, Integer.toString(slot + 1), r.x + 2, r.y + 10);
    }

    private void drawSelectionBorder(Graphics2D g, Rectangle r) {
        g.setColor(new Color(255, 230, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(r.x - 1, r.y - 1, r.width + 2, r.height + 2, 6, 6);
        g.setStroke(new BasicStroke(1));
    }

    private void drawKeyLabel(Graphics2D g, String text, int x, int y) {
        g.setFont(UI.font(9));
        UI.shadowText(g, text, x, y, Color.WHITE);
    }

    // Progress wave di pojok kanan bawah: terisi dari kanan ke kiri, bendera = huge wave
    private void drawProgress(Graphics2D g, Board b) {
        Level level = b.getLevel();
        WaveSpawner spawner = b.getWaveSpawner();
        Rectangle r = PROGRESS;
        String title = level.isEndless() ? "Endless - wave " + spawner.getWavesSpawned() : level.getTitle();
        g.setFont(UI.font(11));
        int tw = g.getFontMetrics().stringWidth(title);
        UI.shadowText(g, title, r.x - tw - 8, r.y + 11, new Color(255, 240, 200));

        g.setColor(new Color(40, 25, 10, 220));
        g.fillRoundRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4, 10, 10);
        g.setColor(new Color(90, 70, 40));
        g.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
        int fill = (int) (r.width * spawner.getProgress());
        g.setColor(new Color(110, 200, 60));
        g.fillRoundRect(r.x + r.width - fill, r.y, fill, r.height, 8, 8);

        int total = level.isEndless() ? Level.ENDLESS_FLAG_EVERY : level.getWaveCount();
        int base = level.isEndless() ? spawner.getWavesSpawned() / Level.ENDLESS_FLAG_EVERY * Level.ENDLESS_FLAG_EVERY : 0;
        for (int i = 0; i < total; i++) {
            if (level.isFlagWave(base + i)) {
                int fx = r.x + r.width - (int) (r.width * (i + 1.0) / total);
                boolean reached = base + i < spawner.getWavesSpawned();
                g.setColor(new Color(60, 40, 20));
                g.fillRect(fx + 3, r.y - 12, 2, 20);
                g.setColor(reached ? new Color(120, 120, 120) : new Color(220, 40, 30));
                g.fillPolygon(new int[] { fx + 5, fx + 15, fx + 5 }, new int[] { r.y - 12, r.y - 8, r.y - 4 }, 3);
            }
        }
        Image head = Assets.sprite("zombie_head").frame(0);
        int hx = r.x + r.width - fill - 10;
        g.drawImage(head, hx, r.y - 6, 22, 22, null);
    }

    private void drawBanners(Graphics2D g, Board b) {
        if (introTicks > 0) {
            int elapsed = INTRO_TICKS - introTicks;
            int part = INTRO_TICKS / 3;
            if (elapsed < part) {
                UI.outlinedTitle(g, "Ready...", Board.WIDTH / 2, 215, 40, new Color(255, 70, 50), Color.BLACK);
            } else if (elapsed < 2 * part) {
                UI.outlinedTitle(g, "Set...", Board.WIDTH / 2, 215, 40, new Color(255, 70, 50), Color.BLACK);
            } else {
                double pop = Math.min(1, (elapsed - 2 * part) / 8.0);
                UI.outlinedTitle(g, "PLANT!", Board.WIDTH / 2, 225, (int) (40 + 24 * pop), new Color(255, 50, 30),
                        Color.BLACK);
            }
            return;
        }
        switch (b.getWaveSpawner().getBanner()) {
            case HUGE_WAVE:
                if ((b.getTicks() / 20) % 2 == 0) {
                    g.drawImage(Assets.get("image/ui/huge_wave_of_zombies_text.png", 560, 30), 50, 190, null);
                }
                break;
            case FINAL_WAVE:
                UI.outlinedTitle(g, "FINAL WAVE", Board.WIDTH / 2, 222, 48, new Color(230, 30, 20), Color.BLACK);
                break;
            default:
                break;
        }
        if (b.getResult() == Board.Result.WON) {
            UI.dim(g, Math.min(120, endTicks * 3));
            UI.outlinedTitle(g, "LEVEL SELESAI!", Board.WIDTH / 2, 215, 42, new Color(255, 230, 90), UI.WOOD_DARK);
        } else if (b.getResult() == Board.Result.LOST) {
            UI.dim(g, Math.min(170, endTicks * 4));
            UI.outlinedTitle(g, "ZOMBIE MASUK RUMAH!", Board.WIDTH / 2, 215, 34, new Color(140, 230, 90),
                    Color.BLACK);
        }
    }

    private void drawPauseOverlay(Graphics2D g) {
        UI.dim(g, 150);
        UI.woodPanel(g, 225, 80, 210, 250);
        UI.outlinedTitle(g, "PAUSE", Board.WIDTH / 2, 130, 34, new Color(255, 230, 120), UI.WOOD_DARK);
        SaveData save = game.getSaveData();
        soundButton.setText("Suara " + (save.isSoundOn() ? "ON" : "OFF"));
        musicButton.setText("Musik " + (save.isMusicOn() ? "ON" : "OFF"));
        for (MyButton button : pauseMenu) {
            button.draw(g);
        }
        Font hint = UI.plain(11);
        UI.centered(g, "P / Esc untuk lanjut", Board.WIDTH / 2, 324, hint, new Color(240, 230, 210));
    }
}
