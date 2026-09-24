package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import entity.plant.PlantType;
import entity.zombie.ZombieType;

/**
 * Definisi satu level: siang/malam, jumlah wave, wave mana yang huge wave (bendera),
 * zombie yang boleh muncul, dan tanaman hadiah saat menang.
 *
 * Isi tiap wave disusun dari "poin": wave ke-i punya poin base + growth * i
 * (huge wave x2,5), lalu diisi zombie acak sampai poin habis. Random-nya memakai
 * seed tetap per level, jadi level yang sama selalu punya wave yang sama.
 */
public final class Level {
    public static final int ENDLESS_FLAG_EVERY = 10;

    // Tanaman yang sudah dimiliki sejak awal
    public static final List<PlantType> STARTING_PLANTS = List.of(PlantType.PEASHOOTER, PlantType.SUNFLOWER,
            PlantType.CHERRY_BOMB, PlantType.WALL_NUT, PlantType.POTATO_MINE, PlantType.SQUASH);

    public static final List<Level> ADVENTURE = List.of(
            new Level(1, "Halaman Depan", false, 6, Set.of(6), 1.0, 0.6,
                    List.of(ZombieType.NORMAL, ZombieType.CONEHEAD),
                    List.of(PlantType.SNOW_PEA, PlantType.LILY_PAD)),
            new Level(2, "Pesta Kolam", false, 8, Set.of(4, 8), 1.5, 0.7,
                    List.of(ZombieType.NORMAL, ZombieType.CONEHEAD, ZombieType.NEWSPAPER, ZombieType.POLE_VAULTING,
                            ZombieType.DUCKY_TUBE),
                    List.of(PlantType.TANGLE_KELP, PlantType.REPEATER)),
            new Level(3, "Serbuan Siang", false, 10, Set.of(5, 10), 2.0, 0.8,
                    List.of(ZombieType.NORMAL, ZombieType.CONEHEAD, ZombieType.BUCKETHEAD, ZombieType.NEWSPAPER,
                            ZombieType.POLE_VAULTING, ZombieType.DUCKY_TUBE, ZombieType.DUCKY_TUBE_CONEHEAD,
                            ZombieType.SNORKEL),
                    List.of(PlantType.PUFF_SHROOM, PlantType.SUN_SHROOM)),
            new Level(4, "Malam Pertama", true, 8, Set.of(4, 8), 1.5, 0.7,
                    List.of(ZombieType.NORMAL, ZombieType.CONEHEAD, ZombieType.NEWSPAPER, ZombieType.POLE_VAULTING,
                            ZombieType.DUCKY_TUBE, ZombieType.SNORKEL),
                    List.of(PlantType.FUME_SHROOM, PlantType.TALL_NUT)),
            new Level(5, "Lumba-lumba Malam", true, 10, Set.of(5, 10), 2.0, 0.9,
                    List.of(ZombieType.NORMAL, ZombieType.CONEHEAD, ZombieType.BUCKETHEAD, ZombieType.FOOTBALL,
                            ZombieType.NEWSPAPER, ZombieType.DUCKY_TUBE, ZombieType.DUCKY_TUBE_CONEHEAD,
                            ZombieType.DOLPHIN_RIDER),
                    List.of(PlantType.JALAPENO)),
            new Level(6, "Serangan Terakhir", false, 12, Set.of(4, 8, 12), 2.5, 1.0,
                    List.of(ZombieType.values()), List.of()));

    public static final Level ENDLESS = new Level(0, "Endless", false, -1, Set.of(), 3.0, 0.9,
            List.of(ZombieType.values()), List.of());

    private final int number; // 1..6, 0 = endless
    private final String name;
    private final boolean night;
    private final int waveCount; // -1 = tidak terbatas
    private final Set<Integer> flagWaves; // nomor wave (mulai 1)
    private final double basePoints, pointGrowth;
    private final List<ZombieType> zombieTypes;
    private final List<PlantType> rewards;
    private final List<Wave> waves = new ArrayList<>();
    private final Random random;

    private Level(int number, String name, boolean night, int waveCount, Set<Integer> flagWaves, double basePoints,
            double pointGrowth, List<ZombieType> zombieTypes, List<PlantType> rewards) {
        this.number = number;
        this.name = name;
        this.night = night;
        this.waveCount = waveCount;
        this.flagWaves = flagWaves;
        this.basePoints = basePoints;
        this.pointGrowth = pointGrowth;
        this.zombieTypes = zombieTypes;
        this.rewards = rewards;
        this.random = new Random(1000L + number);
    }

    // ------------------------------------------------------------------ wave

    public synchronized Wave getWave(int index) {
        while (waves.size() <= index) {
            waves.add(generateWave(waves.size()));
        }
        return waves.get(index);
    }

    private Wave generateWave(int index) {
        boolean flag = isFlagWave(index);
        double points = basePoints + pointGrowth * index;
        if (flag) {
            points = points * 2.5 + 2;
        }
        // zombie mahal (> 2 poin) baru muncul setelah sepertiga level
        int strongFrom = isEndless() ? 4 : Math.max(1, waveCount / 3);
        List<ZombieType> result = new ArrayList<>();
        while (true) {
            List<ZombieType> affordable = new ArrayList<>();
            for (ZombieType type : zombieTypes) {
                if (type.getPoints() <= points && (type.getPoints() <= 2 || index >= strongFrom)) {
                    affordable.add(type);
                }
            }
            if (affordable.isEmpty()) {
                break;
            }
            ZombieType chosen = affordable.get(random.nextInt(affordable.size()));
            result.add(chosen);
            points -= chosen.getPoints();
        }
        if (result.isEmpty()) {
            result.add(zombieTypes.get(0));
        }
        return new Wave(Collections.unmodifiableList(result), flag);
    }

    public boolean isFlagWave(int index) {
        return isEndless() ? (index + 1) % ENDLESS_FLAG_EVERY == 0 : flagWaves.contains(index + 1);
    }

    public boolean isFinalWave(int index) {
        return !isEndless() && index == waveCount - 1;
    }

    // ------------------------------------------------------------------ getter

    public boolean isEndless() {
        return waveCount < 0;
    }

    public int getNumber() {
        return number;
    }

    public int getIndex() {
        return number - 1;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return isEndless() ? "Endless" : "Level " + number + " - " + name;
    }

    public boolean isNight() {
        return night;
    }

    public int getWaveCount() {
        return waveCount;
    }

    public List<ZombieType> getZombieTypes() {
        return zombieTypes;
    }

    public List<PlantType> getRewards() {
        return rewards;
    }

    public boolean hasAquaticZombies() {
        return zombieTypes.stream().anyMatch(ZombieType::isAquatic);
    }

    public Level next() {
        return isEndless() || number >= ADVENTURE.size() ? null : ADVENTURE.get(number);
    }
}
