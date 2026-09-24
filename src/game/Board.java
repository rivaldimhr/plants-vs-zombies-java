package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.random.RandomGenerator;

import entity.plant.LilyPad;
import entity.plant.Plant;
import entity.plant.PlantType;
import entity.projectile.Bullet;
import entity.zombie.Zombie;

/**
 * Seluruh state satu permainan: tanaman, zombie, peluru, sun, dan waktu.
 * Tidak tahu apa-apa soal tampilan/input, jadi bisa diuji tanpa layar.
 * Satu game baru = satu objek Board baru.
 */
public class Board {
    // Grid
    public static final int TILE_SIZE = 60;
    public static final int COLS = 11;
    public static final int ROWS = 7; // baris 0 = HUD, baris 1-6 = halaman
    public static final int WIDTH = TILE_SIZE * COLS;
    public static final int HEIGHT = TILE_SIZE * ROWS;

    // Waktu (detik)
    public static final int GAME_DURATION = 240; // setelah ini zombie berhenti muncul
    public static final int DAY_NIGHT_CYCLE = 200;
    public static final int DAY_LENGTH = 100; // detik 0-99 tiap siklus = siang

    public enum Result {
        PLAYING, WON, LOST
    }

    // CopyOnWriteArrayList: dibaca thread render sambil diubah thread game
    private final List<Plant> plants = new CopyOnWriteArrayList<>();
    private final List<Zombie> zombies = new CopyOnWriteArrayList<>();
    private final List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private final Sun sun;
    private final ZombieSpawner spawner;
    private volatile long ticks = 0;
    private volatile Result result = Result.PLAYING;
    private boolean spawningEnabled = true;

    public Board(RandomGenerator random) {
        this.sun = new Sun(random);
        this.spawner = new ZombieSpawner(random);
    }

    // ------------------------------------------------------------------ loop

    // Dipanggil 60x per detik
    public void update() {
        if (result != Result.PLAYING) {
            return;
        }
        ticks++;
        if (ticks % Game.UPS == 0 && spawningEnabled) {
            spawner.onSecond(this);
        }
        if (isDay()) {
            sun.update();
        }

        for (Bullet bullet : bullets) {
            bullet.update(this);
        }
        for (Zombie zombie : zombies) {
            zombie.update(this);
        }
        for (Plant plant : plants) {
            plant.update(this);
        }
        bullets.removeIf(Bullet::isDone);
        zombies.removeIf(Zombie::isDead);
        plants.removeIf(Plant::isDead);

        checkResult();
    }

    private void checkResult() {
        for (Zombie zombie : zombies) {
            if (zombie.getX() <= TILE_SIZE) {
                result = Result.LOST;
                return;
            }
        }
        if (getTime() >= GAME_DURATION && zombies.isEmpty()) {
            result = Result.WON;
        }
    }

    // ------------------------------------------------------------------ tanam

    public static boolean isWaterRow(int row) {
        return row == 3 || row == 4;
    }

    public static boolean isInsideLawn(int col, int row) {
        // kolom 0 = rumah, kolom terakhir = tempat zombie muncul
        return col >= 1 && col <= COLS - 2 && row >= 1 && row <= ROWS - 1;
    }

    public List<Plant> getPlantsAt(int col, int row) {
        List<Plant> result = new ArrayList<>();
        for (Plant plant : plants) {
            if (plant.getCol() == col && plant.getRow() == row) {
                result.add(plant);
            }
        }
        return result;
    }

    /*
     * Aturan tanam (tidak memeriksa sun & cooldown):
     * - hanya di dalam halaman (bukan kolom rumah / spawn zombie)
     * - tile kosong: tanaman air hanya di kolam, tanaman darat hanya di darat
     * - di atas Lily Pad hanya bisa tanaman darat
     */
    public boolean canPlant(PlantType type, int col, int row) {
        if (!isInsideLawn(col, row)) {
            return false;
        }
        List<Plant> onTile = getPlantsAt(col, row);
        if (onTile.isEmpty()) {
            return type.isAquatic() == isWaterRow(row);
        }
        return onTile.size() == 1 && onTile.get(0) instanceof LilyPad && !type.isAquatic();
    }

    // Tanam kalau aturan terpenuhi dan sun cukup. Return true kalau berhasil.
    public boolean plant(PlantType type, int col, int row) {
        if (!canPlant(type, col, row) || !sun.spend(type.getCost())) {
            return false;
        }
        plants.add(type.create(col * TILE_SIZE, row * TILE_SIZE));
        return true;
    }

    // Sekop: cabut semua tanaman di tile. Return true kalau ada yang dicabut.
    public boolean dig(int col, int row) {
        return plants.removeIf(p -> p.getCol() == col && p.getRow() == row);
    }

    // ------------------------------------------------------------------ waktu

    public int getTime() {
        return (int) (ticks / Game.UPS);
    }

    public long getTicks() {
        return ticks;
    }

    public boolean isDay() {
        return getTime() % DAY_NIGHT_CYCLE < DAY_LENGTH;
    }

    public boolean isHugeWave() {
        int time = getTime();
        return time >= ZombieSpawner.HUGE_WAVE_START && time < GAME_DURATION;
    }

    // ------------------------------------------------------------------ getter & setter

    public List<Plant> getPlants() {
        return Collections.unmodifiableList(plants);
    }

    public List<Zombie> getZombies() {
        return Collections.unmodifiableList(zombies);
    }

    public List<Bullet> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    public void addPlant(Plant plant) {
        plants.add(plant);
    }

    public void addZombie(Zombie zombie) {
        zombies.add(zombie);
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public Sun getSun() {
        return sun;
    }

    public Result getResult() {
        return result;
    }

    // Untuk pengujian: matikan spawn zombie acak
    public void setSpawningEnabled(boolean spawningEnabled) {
        this.spawningEnabled = spawningEnabled;
    }

    // Untuk pengujian: lompat ke detik tertentu
    public void setTime(int seconds) {
        ticks = (long) seconds * Game.UPS;
    }
}
