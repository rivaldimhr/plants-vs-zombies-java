import java.awt.Frame;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import entity.SunToken;
import entity.plant.Plant;
import entity.zombie.Zombie;
import game.Board;
import game.Game;
import game.States;

/**
 * Pemain otomatis untuk merekam GIF gameplay dan menjadi test end-to-end versi desktop.
 *
 * Menjalankan game sungguhan, lalu menggerakkan mouse sungguhan (java.awt.Robot) seperti pemain:
 * Start → Level 1 → pilih 6 tanaman → Play → kumpulkan sun, tanam Sunflower dan Peashooter, pasang Wall-nut.
 * Posisi sun dan zombie dibaca dari state game supaya kliknya tepat; semua aksi tetap lewat klik mouse.
 *
 * Butuh layar (di server: xvfb-run). Contoh:
 *   javac -cp dist/plants-vs-zombies.jar -d tools/demo/out tools/demo/DemoPlayer.java
 *   xvfb-run java -cp dist/plants-vs-zombies.jar:tools/demo/out DemoPlayer 60
 *
 * Kode keluar 0 bila permainan berjalan normal (lihat ringkasan JSON di akhir), 1 bila tidak.
 */
public class DemoPlayer {
    // Koordinat di dalam jendela game (skala 1x, 660x420)
    private static final Point MENU_START = new Point(328, 322);
    private static final Point LEVEL_1 = new Point(123, 120);
    // Urutan klik di inventory = urutan slot di deck: Sunflower, Peashooter, Wall-nut, Cherry Bomb, Potato Mine, Squash
    private static final Point[] INVENTORY_PICKS = {
        new Point(208, 185), new Point(124, 185), new Point(376, 185),
        new Point(292, 185), new Point(460, 185), new Point(208, 265) };
    private static final Point PLAY = new Point(607, 22);
    private static final int SLOT_SUNFLOWER = 0, SLOT_PEASHOOTER = 1, SLOT_WALLNUT = 2, SLOT_CHERRY = 3;
    private static final int[] LAND_ROWS = { 1, 2, 5, 6 };

    private final Robot robot;
    private final Game game;
    private final AtomicInteger errors = new AtomicInteger();
    private int sunsClicked = 0, planted = 0;

    private DemoPlayer(Robot robot, Game game) {
        this.robot = robot;
        this.game = game;
    }

    public static void main(String[] args) throws Exception {
        int seconds = args.length > 0 ? Integer.parseInt(args[0]) : 60;
        AtomicInteger uncaught = new AtomicInteger();
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            uncaught.incrementAndGet();
            e.printStackTrace();
        });

        Game.main(new String[0]);
        Game game = waitForGame();
        Robot robot = new Robot();
        robot.setAutoDelay(15);
        DemoPlayer p = new DemoPlayer(robot, game);
        p.errors.set(0);
        boolean ok = p.play(seconds);

        Board board = game.getGameLevel().getBoard();
        String summary = String.format(
            "{\"reachedGame\": %b, \"state\": \"%s\", \"sunsClicked\": %d, \"planted\": %d, \"plantsOnBoard\": %d, "
                + "\"zombiesKilled\": %d, \"result\": \"%s\", \"uncaughtExceptions\": %d}",
            ok, game.getStates(), p.sunsClicked, p.planted, board == null ? 0 : board.getPlants().size(),
            board == null ? 0 : board.getZombiesKilled(), board == null ? "-" : board.getResult(), uncaught.get());
        System.out.println("DEMO_SUMMARY " + summary);

        boolean pass = ok && uncaught.get() == 0 && p.sunsClicked >= 3 && p.planted >= 4
            && board != null && board.getZombiesKilled() >= 1;
        System.exit(pass ? 0 : 1);
    }

    private static Game waitForGame() throws InterruptedException {
        for (int i = 0; i < 100; i++) {
            for (Frame f : Frame.getFrames()) {
                if (f instanceof Game g && g.isShowing()) {
                    Thread.sleep(1500); // beri waktu menu tampil
                    return g;
                }
            }
            Thread.sleep(100);
        }
        throw new IllegalStateException("Jendela game tidak muncul");
    }

    private boolean play(int seconds) throws InterruptedException {
        if (!clickUntil(MENU_START, States.LEVEL_SELECT) || !clickUntil(LEVEL_1, States.INVENTORY)) {
            System.err.println("Gagal berpindah layar, state: " + game.getStates());
            return false;
        }
        for (Point pick : INVENTORY_PICKS) {
            click(pick, 350);
        }
        clickUntil(PLAY, States.GAME_LEVEL);
        if (game.getStates() != States.GAME_LEVEL) {
            System.err.println("Tidak masuk ke permainan, state: " + game.getStates());
            return false;
        }
        Thread.sleep(2800); // intro "Ready... Set... Plant!"
        robot.keyPress(KeyEvent.VK_F); // kecepatan 2x supaya rekaman lebih padat
        robot.keyRelease(KeyEvent.VK_F);

        long end = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < end && game.getStates() == States.GAME_LEVEL) {
            Board board = game.getGameLevel().getBoard();
            if (board.getResult() != Board.Result.PLAYING) {
                break;
            }
            collectSun(board);
            plantSomething(board);
            Thread.sleep(250);
        }
        return true;
    }

    // Klik lalu tunggu sampai layar berpindah; ulangi bila klik jatuh saat game sedang sibuk (mis. server CI lambat)
    private boolean clickUntil(Point p, States expected) throws InterruptedException {
        for (int attempt = 0; attempt < 5; attempt++) {
            click(p, 0);
            for (int i = 0; i < 30 && game.getStates() != expected; i++) {
                Thread.sleep(100);
            }
            if (game.getStates() == expected) {
                Thread.sleep(500);
                return true;
            }
        }
        return false;
    }

    private void collectSun(Board board) throws InterruptedException {
        List<SunToken> suns = new ArrayList<>(board.getSuns());
        for (SunToken s : suns) {
            if (s.isCollectable()) {
                click(new Point((int) s.getX(), (int) s.getY()), 180);
                sunsClicked++;
            }
        }
    }

    private void plantSomething(Board board) throws InterruptedException {
        int sun = board.getSun().get();
        int sunflowers = count(board, "Sunflower");
        int shooters = count(board, "Peashooter");

        // Cherry Bomb bila 2+ zombie berkerumun di satu baris
        for (int row : LAND_ROWS) {
            if (zombiesIn(board, row) >= 2 && ready(SLOT_CHERRY) && sun >= 150) {
                int col = Math.max(4, Math.min(9, nearestZombieX(board, row) / Board.TILE_SIZE));
                if (board.canPlant(game.getDeck().get(SLOT_CHERRY), col, row)) {
                    plant(SLOT_CHERRY, col, row);
                    return;
                }
            }
        }
        // Prioritas: baris yang ada zombienya tapi belum ada Peashooter
        for (int row : LAND_ROWS) {
            if (zombiesIn(board, row) > 0 && !hasPlant(board, "Peashooter", row) && ready(SLOT_PEASHOOTER) && sun >= 100) {
                plant(SLOT_PEASHOOTER, 3, row);
                return;
            }
        }
        // Wall-nut di depan baris yang zombienya sudah dekat
        for (int row : LAND_ROWS) {
            if (nearestZombieX(board, row) < 450 && !hasPlant(board, "WallNut", row) && ready(SLOT_WALLNUT) && sun >= 50
                && board.canPlant(game.getDeck().get(SLOT_WALLNUT), 5, row)) {
                plant(SLOT_WALLNUT, 5, row);
                return;
            }
        }
        // Ekonomi: satu Sunflower per baris darat
        if (sunflowers < LAND_ROWS.length && ready(SLOT_SUNFLOWER) && sun >= 50 && sunflowers <= shooters + 1) {
            for (int row : LAND_ROWS) {
                if (!hasPlant(board, "Sunflower", row)) {
                    plant(SLOT_SUNFLOWER, 1, row);
                    return;
                }
            }
        }
        // Sun berlebih: Peashooter kedua di baris yang ada zombienya
        if (ready(SLOT_PEASHOOTER) && sun >= 250) {
            for (int row : LAND_ROWS) {
                if (zombiesIn(board, row) > 0 && board.canPlant(game.getDeck().get(SLOT_PEASHOOTER), 2, row)) {
                    plant(SLOT_PEASHOOTER, 2, row);
                    return;
                }
            }
        }
        // Lengkapi Peashooter di baris darat yang belum punya
        if (ready(SLOT_PEASHOOTER) && sun >= 100) {
            for (int row : LAND_ROWS) {
                if (!hasPlant(board, "Peashooter", row)) {
                    plant(SLOT_PEASHOOTER, 3, row);
                    return;
                }
            }
        }
    }

    private void plant(int slot, int col, int row) throws InterruptedException {
        Board board = game.getGameLevel().getBoard();
        if (!board.canPlant(game.getDeck().get(slot), col, row)) {
            return;
        }
        click(cardCenter(slot), 220);
        click(new Point(col * Board.TILE_SIZE + Board.TILE_SIZE / 2, row * Board.TILE_SIZE + Board.TILE_SIZE / 2), 250);
        planted++;
    }

    private boolean ready(int slot) {
        return game.getDeck().isReady(slot);
    }

    private static Point cardCenter(int slot) {
        // SLOT_X/SLOT_Y di GameLevel (koordinat deck.png) dikali BANK_SCALE (66/204)
        int[] slotX = { 203, 333, 463, 597, 737, 870 };
        double s = 66 / 204.0; // BANK_SCALE di GameLevel
        return new Point((int) Math.round((slotX[slot] + 46) * s), (int) Math.round((37 + 70) * s));
    }

    private static int count(Board board, String type) {
        int n = 0;
        for (Plant p : new ArrayList<>(board.getPlants())) {
            if (p.getClass().getSimpleName().equals(type)) {
                n++;
            }
        }
        return n;
    }

    private static boolean hasPlant(Board board, String type, int row) {
        for (Plant p : new ArrayList<>(board.getPlants())) {
            if (p.getRow() == row && p.getClass().getSimpleName().equals(type)) {
                return true;
            }
        }
        return false;
    }

    private static int zombiesIn(Board board, int row) {
        int n = 0;
        for (Zombie z : new ArrayList<>(board.getZombies())) {
            if (z.getRow() == row) {
                n++;
            }
        }
        return n;
    }

    private static int nearestZombieX(Board board, int row) {
        int best = Integer.MAX_VALUE;
        for (Zombie z : new ArrayList<>(board.getZombies())) {
            if (z.getRow() == row) {
                best = Math.min(best, z.getX());
            }
        }
        return best;
    }

    private void click(Point inGame, int waitMs) throws InterruptedException {
        Point origin = game.getGameScreen().getLocationOnScreen();
        double scale = game.getSaveData().getWindowScale();
        int x = origin.x + (int) Math.round(inGame.x * scale);
        int y = origin.y + (int) Math.round(inGame.y * scale);
        robot.mouseMove(x, y);
        Thread.sleep(60);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(waitMs);
    }
}
