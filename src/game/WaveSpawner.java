package game;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

import entity.zombie.Zombie;
import entity.zombie.ZombieType;

/**
 * Mengeluarkan wave zombie seperti di PvZ:
 * - wave pertama setelah 20 detik (waktu persiapan)
 * - wave berikutnya tiap 25 detik, atau lebih cepat kalau zombie wave
 *   sebelumnya sudah hampir habis (HP tersisa < 35%)
 * - sebelum huge wave muncul tulisan "A huge wave of zombies is approaching!"
 * - wave terakhir diberi tulisan "FINAL WAVE"
 */
public class WaveSpawner {
    public static final int FIRST_WAVE = 20 * Game.UPS;
    public static final int WAVE_INTERVAL = 25 * Game.UPS;
    public static final int MIN_WAVE_GAP = 6 * Game.UPS;
    public static final int WARNING = 5 * Game.UPS; // jarak tulisan huge wave sebelum wave keluar
    public static final int BANNER_TIME = 4 * Game.UPS;
    public static final double EARLY_HEALTH = 0.35;
    private static final int[] LAND_ROWS = { 1, 2, 5, 6 };
    private static final int[] WATER_ROWS = { 3, 4 };

    public enum Banner {
        NONE, HUGE_WAVE, FINAL_WAVE
    }

    private final Level level;
    private final RandomGenerator random;
    private int nextWave = 0;
    private int countdown = FIRST_WAVE;
    private int ticksSinceWave = 0;
    private final List<Zombie> lastWave = new ArrayList<>();
    private int lastWaveHealth = 0;
    private volatile Banner banner = Banner.NONE;
    private volatile int bannerTicks = 0;

    public WaveSpawner(Level level, RandomGenerator random) {
        this.level = level;
        this.random = random;
    }

    // Dipanggil Board tiap tick
    public void update(Board board) {
        if (bannerTicks > 0 && --bannerTicks == 0) {
            banner = Banner.NONE;
        }
        if (isFinished()) {
            return;
        }
        ticksSinceWave++;
        boolean flagNext = level.isFlagWave(nextWave);
        if (nextWave > 0 && ticksSinceWave >= MIN_WAVE_GAP && countdown > WARNING + 1
                && remainingHealthFraction() < EARLY_HEALTH) {
            countdown = flagNext ? WARNING + 1 : 1;
        }
        countdown--;
        if (countdown == WARNING && flagNext) {
            showBanner(Banner.HUGE_WAVE);
            board.fire(GameEvent.HUGE_WAVE);
        }
        if (countdown <= 0) {
            spawnWave(board);
        }
    }

    private void spawnWave(Board board) {
        Wave wave = level.getWave(nextWave);
        if (level.isFinalWave(nextWave)) {
            showBanner(Banner.FINAL_WAVE);
            board.fire(GameEvent.FINAL_WAVE);
        } else {
            board.fire(GameEvent.WAVE);
        }
        lastWave.clear();
        lastWaveHealth = 0;
        for (ZombieType type : wave.zombies()) {
            int[] rows = type.isAquatic() ? WATER_ROWS : LAND_ROWS;
            int row = rows[random.nextInt(rows.length)];
            int x = (Board.COLS - 1) * Board.TILE_SIZE + random.nextInt(0, 50);
            Zombie zombie = type.create(x, row * Board.TILE_SIZE);
            board.addZombie(zombie);
            lastWave.add(zombie);
            lastWaveHealth += zombie.getTotalHealth();
        }
        nextWave++;
        countdown = WAVE_INTERVAL;
        ticksSinceWave = 0;
    }

    private void showBanner(Banner type) {
        banner = type;
        bannerTicks = BANNER_TIME;
    }

    // Sisa HP zombie wave terakhir (0..1)
    double remainingHealthFraction() {
        if (lastWaveHealth <= 0) {
            return 0;
        }
        int remaining = 0;
        for (Zombie zombie : lastWave) {
            if (!zombie.isDead()) {
                remaining += Math.max(0, zombie.getTotalHealth());
            }
        }
        return (double) remaining / lastWaveHealth;
    }

    public boolean isFinished() {
        return !level.isEndless() && nextWave >= level.getWaveCount();
    }

    public int getWavesSpawned() {
        return nextWave;
    }

    // 0..1 untuk progress bar (endless: progres menuju bendera berikutnya)
    public double getProgress() {
        if (level.isEndless()) {
            return (nextWave % Level.ENDLESS_FLAG_EVERY) / (double) Level.ENDLESS_FLAG_EVERY;
        }
        return (double) nextWave / level.getWaveCount();
    }

    public Banner getBanner() {
        return banner;
    }

    public int getCountdown() {
        return countdown;
    }

    // Untuk pengujian: anggap semua wave sudah keluar
    void markAllWavesSpawned() {
        nextWave = Math.max(0, level.getWaveCount());
    }
}
