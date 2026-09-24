package game;

import java.util.Random;

import entity.SunToken;

// Helper untuk test: board tanpa spawn acak dan fungsi menjalankan tick
public final class TestUtil {
    private TestUtil() {
    }

    // Board level siang tanpa zombie/sun acak
    public static Board quietBoard() {
        return quietBoard(Level.ADVENTURE.get(0));
    }

    public static Board quietBoard(Level level) {
        Board board = new Board(level, new Random(42));
        board.setSpawningEnabled(false);
        return board;
    }

    // Board level malam (jamur aktif, tidak ada sun dari langit)
    public static Board nightBoard() {
        return quietBoard(Level.ADVENTURE.get(3));
    }

    public static void tick(Board board, int ticks) {
        for (int i = 0; i < ticks; i++) {
            board.update();
        }
    }

    public static void seconds(Board board, double seconds) {
        tick(board, (int) Math.round(seconds * Game.UPS));
    }

    public static int px(int tile) {
        return tile * Board.TILE_SIZE;
    }

    // Tunggu sampai semua sun yang sedang jatuh mendarat
    public static void landSuns(Board board) {
        for (int i = 0; i < 20 * Game.UPS; i++) {
            boolean falling = board.getSuns().stream().anyMatch(s -> s.getState() == SunToken.State.FALLING);
            if (!falling) {
                return;
            }
            board.update();
        }
    }

    // Tandai semua wave sudah keluar (supaya level bisa dimenangkan di test)
    public static void finishWaves(Board board) {
        board.getWaveSpawner().markAllWavesSpawned();
    }
}
