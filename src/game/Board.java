package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.random.RandomGenerator;

import entity.LawnMower;
import entity.SunToken;
import entity.effect.Effect;
import entity.plant.LilyPad;
import entity.plant.Plant;
import entity.plant.PlantType;
import entity.projectile.Bullet;
import entity.zombie.Armor;
import entity.zombie.Zombie;

/**
 * Seluruh state satu permainan: tanaman, zombie, peluru, sun, lawn mower, efek,
 * dan wave. Tidak tahu apa-apa soal tampilan/input/suara (suara lewat event),
 * jadi bisa diuji tanpa layar. Satu permainan baru = satu objek Board baru.
 */
public class Board {
    // Grid
    public static final int TILE_SIZE = 60;
    public static final int COLS = 11;
    public static final int ROWS = 7; // baris 0 = HUD, baris 1-6 = halaman
    public static final int WIDTH = TILE_SIZE * COLS;
    public static final int HEIGHT = TILE_SIZE * ROWS;

    public static final int LOSE_X = -25; // zombie melewati garis ini = rumah dimasuki
    public static final int SKY_SUN_VALUE = 25;
    public static final int SKY_SUN_FIRST = 4 * Game.UPS;
    public static final int SKY_SUN_MIN = 7 * Game.UPS;
    public static final int SKY_SUN_MAX = 11 * Game.UPS;
    private static final int GROAN_CHANCE = 7 * Game.UPS; // rata-rata satu erangan tiap ~7 detik

    public enum Result {
        PLAYING, WON, LOST
    }

    // CopyOnWriteArrayList: dibaca thread render sambil diubah thread game
    private final List<Plant> plants = new CopyOnWriteArrayList<>();
    private final List<Zombie> zombies = new CopyOnWriteArrayList<>();
    private final List<Bullet> bullets = new CopyOnWriteArrayList<>();
    private final List<SunToken> suns = new CopyOnWriteArrayList<>();
    private final List<Effect> effects = new CopyOnWriteArrayList<>();
    private final List<LawnMower> mowers = new ArrayList<>();
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Level level;
    private final RandomGenerator random;
    private final Sun sun = new Sun();
    private final WaveSpawner spawner;
    private volatile long ticks = 0;
    private volatile Result result = Result.PLAYING;
    private boolean spawningEnabled = true;
    private int skySunTimer = SKY_SUN_FIRST;
    private int zombiesKilled = 0;

    public Board(Level level, RandomGenerator random) {
        this.level = level;
        this.random = random;
        this.spawner = new WaveSpawner(level, random);
        for (int row = 1; row < ROWS; row++) {
            mowers.add(new LawnMower(row));
        }
    }

    // Board level 1 (untuk pengujian)
    public Board(RandomGenerator random) {
        this(Level.ADVENTURE.get(0), random);
    }

    // ------------------------------------------------------------------ loop

    // Dipanggil 60x per detik (2x lipat saat kecepatan 2x)
    public void update() {
        if (result != Result.PLAYING) {
            return;
        }
        ticks++;
        if (spawningEnabled) {
            spawner.update(this);
            if (isDay()) {
                updateSkySun();
            }
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
        for (SunToken token : suns) {
            token.update(this);
        }
        for (LawnMower mower : mowers) {
            mower.update(this);
        }
        for (Effect effect : effects) {
            effect.update();
        }
        for (Plant plant : plants) {
            plant.tickAnimation();
        }
        for (Zombie zombie : zombies) {
            zombie.tickAnimation();
            Armor lost = zombie.pollDroppedArmor();
            if (lost != null) {
                effects.add(zombie.createArmorDropEffect(lost));
                fire(GameEvent.ARMOR_LOST);
            }
        }

        removeDead();
        if (!zombies.isEmpty() && random.nextInt(GROAN_CHANCE) == 0) {
            fire(GameEvent.GROAN);
        }
        checkResult();
    }

    private void removeDead() {
        List<Zombie> dead = new ArrayList<>();
        for (Zombie zombie : zombies) {
            if (zombie.isDead()) {
                dead.add(zombie);
            }
        }
        for (Zombie zombie : dead) {
            effects.add(zombie.createDeathEffect());
            zombiesKilled++;
            fire(GameEvent.ZOMBIE_DIED);
        }
        zombies.removeAll(dead);
        plants.removeIf(Plant::isDead);
        bullets.removeIf(Bullet::isDone);
        suns.removeIf(SunToken::isDone);
        effects.removeIf(Effect::isDone);
    }

    private void updateSkySun() {
        if (--skySunTimer > 0) {
            return;
        }
        skySunTimer = random.nextInt(SKY_SUN_MIN, SKY_SUN_MAX + 1);
        double x = random.nextInt(TILE_SIZE + 20, (COLS - 2) * TILE_SIZE);
        double ground = random.nextInt(2, ROWS) * TILE_SIZE - 12;
        suns.add(SunToken.fromSky(x, ground, SKY_SUN_VALUE));
    }

    private void checkResult() {
        for (Zombie zombie : zombies) {
            if (zombie.getX() <= LOSE_X) {
                result = Result.LOST;
                fire(GameEvent.LEVEL_LOST);
                return;
            }
        }
        if (!level.isEndless() && spawner.isFinished() && zombies.isEmpty()) {
            result = Result.WON;
            fire(GameEvent.LEVEL_WON);
        }
    }

    // ------------------------------------------------------------------ event

    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void fire(GameEvent event) {
        for (GameEventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    // ------------------------------------------------------------------ tanam

    public static boolean isWaterRow(int row) {
        return row == 3 || row == 4;
    }

    public static boolean isInsideLawn(int col, int row) {
        // kolom 0 = rumah (lawn mower), kolom terakhir = tempat zombie muncul
        return col >= 1 && col <= COLS - 2 && row >= 1 && row <= ROWS - 1;
    }

    public List<Plant> getPlantsAt(int col, int row) {
        List<Plant> found = new ArrayList<>();
        for (Plant plant : plants) {
            if (plant.getCol() == col && plant.getRow() == row) {
                found.add(plant);
            }
        }
        return found;
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
        fire(GameEvent.PLANT);
        return true;
    }

    // Sekop: cabut semua tanaman di tile. Return true kalau ada yang dicabut.
    public boolean dig(int col, int row) {
        boolean removed = plants.removeIf(p -> p.getCol() == col && p.getRow() == row);
        if (removed) {
            fire(GameEvent.DIG);
        }
        return removed;
    }

    // ------------------------------------------------------------------ sun

    // Ambil sun di titik (x, y). Return true kalau ada sun yang diambil.
    public boolean collectSunAt(double x, double y) {
        for (int i = suns.size() - 1; i >= 0; i--) {
            SunToken token = suns.get(i);
            if (token.contains(x, y)) {
                collect(token);
                return true;
            }
        }
        return false;
    }

    // Ambil semua sun yang ada. Return jumlah sun yang didapat.
    public int collectAllSun() {
        int total = 0;
        for (SunToken token : suns) {
            if (token.isCollectable()) {
                total += token.getValue();
                collect(token);
            }
        }
        return total;
    }

    private void collect(SunToken token) {
        token.collect();
        sun.add(token.getValue());
        fire(GameEvent.SUN_COLLECTED);
    }

    // ------------------------------------------------------------------ waktu

    public int getTime() {
        return (int) (ticks / Game.UPS);
    }

    public long getTicks() {
        return ticks;
    }

    public boolean isDay() {
        return !level.isNight();
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

    public List<SunToken> getSuns() {
        return Collections.unmodifiableList(suns);
    }

    public List<Effect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    public List<LawnMower> getMowers() {
        return Collections.unmodifiableList(mowers);
    }

    public LawnMower getMower(int row) {
        return mowers.get(row - 1);
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

    public void addSun(SunToken token) {
        suns.add(token);
    }

    public void addEffect(Effect effect) {
        if (effect != null) {
            effects.add(effect);
        }
    }

    public Sun getSun() {
        return sun;
    }

    public Level getLevel() {
        return level;
    }

    public WaveSpawner getWaveSpawner() {
        return spawner;
    }

    public int getZombiesKilled() {
        return zombiesKilled;
    }

    public Result getResult() {
        return result;
    }

    // Untuk pengujian: matikan spawn zombie & sun dari langit
    public void setSpawningEnabled(boolean spawningEnabled) {
        this.spawningEnabled = spawningEnabled;
    }

    // Untuk pengujian: lompat ke detik tertentu
    public void setTime(int seconds) {
        ticks = (long) seconds * Game.UPS;
    }
}
