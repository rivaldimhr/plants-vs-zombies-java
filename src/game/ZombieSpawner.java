package game;

import java.util.random.RandomGenerator;

import entity.zombie.ZombieType;

/**
 * Mengatur kapan dan zombie apa yang muncul.
 * - Detik 0-19: belum ada zombie (waktu persiapan)
 * - Detik 20-199: tiap detik ada peluang SPAWN_CHANCE satu zombie muncul
 * - Detik 200-239 (huge wave): 1 zombie langsung muncul di tiap baris, lalu peluang naik
 * - Detik 240+: tidak ada zombie baru
 */
public class ZombieSpawner {
    public static final int SPAWN_START = 20;
    public static final int HUGE_WAVE_START = Board.GAME_DURATION - 40;
    public static final int MAX_ZOMBIES = 10;
    public static final int HUGE_WAVE_MAX_ZOMBIES = 15;
    public static final double SPAWN_CHANCE = 0.3;
    public static final double HUGE_WAVE_CHANCE = 0.7;

    private final RandomGenerator random;

    public ZombieSpawner(RandomGenerator random) {
        this.random = random;
    }

    // Dipanggil Board sekali tiap detik
    public void onSecond(Board board) {
        int time = board.getTime();
        if (time < SPAWN_START || time >= Board.GAME_DURATION) {
            return;
        }
        boolean hugeWave = time >= HUGE_WAVE_START;
        int max = hugeWave ? HUGE_WAVE_MAX_ZOMBIES : MAX_ZOMBIES;

        if (time == HUGE_WAVE_START) {
            for (int row = 1; row < Board.ROWS && board.getZombies().size() < max; row++) {
                spawn(board, row, time);
            }
            return;
        }
        if (board.getZombies().size() < max && random.nextDouble() < (hugeWave ? HUGE_WAVE_CHANCE : SPAWN_CHANCE)) {
            spawn(board, random.nextInt(1, Board.ROWS), time);
        }
    }

    private void spawn(Board board, int row, int time) {
        ZombieType type = ZombieType.random(Board.isWaterRow(row), time, random);
        board.addZombie(type.create((Board.COLS - 1) * Board.TILE_SIZE, row * Board.TILE_SIZE));
    }
}
