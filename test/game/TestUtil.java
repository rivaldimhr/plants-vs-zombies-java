package game;

import java.util.Random;

// Helper untuk test: board tanpa spawn acak dan fungsi menjalankan tick
public final class TestUtil {
    private TestUtil() {
    }

    public static Board quietBoard() {
        Board board = new Board(new Random(42));
        board.setSpawningEnabled(false);
        return board;
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
}
