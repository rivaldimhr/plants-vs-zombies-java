package entity.zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.random.RandomGenerator;

/**
 * Factory semua jenis zombie, beserta aturan spawn-nya.
 * weight  = peluang relatif muncul (makin besar makin sering)
 * minTime = detik paling awal zombie ini boleh muncul (zombie kuat muncul belakangan)
 */
public enum ZombieType {
    NORMAL(NormalZombie::new, 30, 0),
    CONEHEAD(ConeheadZombie::new, 20, 0),
    NEWSPAPER(NewspaperZombie::new, 15, 30),
    POLE_VAULTING(PoleVaultingZombie::new, 12, 40),
    BUCKETHEAD(BucketheadZombie::new, 10, 60),
    FOOTBALL(FootballZombie::new, 8, 90),
    DUCKY_TUBE(DuckyTubeZombie::new, 35, 0),
    DUCKY_TUBE_CONEHEAD(DuckyTubeConeheadZombie::new, 25, 30),
    SNORKEL(SnorkelZombie::new, 20, 40),
    DOLPHIN_RIDER(DolphinRiderZombie::new, 20, 60);

    private final BiFunction<Integer, Integer, Zombie> factory;
    private final int weight;
    private final int minTime;
    private final Zombie prototype;

    ZombieType(BiFunction<Integer, Integer, Zombie> factory, int weight, int minTime) {
        this.factory = factory;
        this.weight = weight;
        this.minTime = minTime;
        this.prototype = factory.apply(0, 0);
    }

    public Zombie create(int x, int y) {
        return factory.apply(x, y);
    }

    public boolean isAquatic() {
        return prototype.isAquatic();
    }

    public String getDisplayName() {
        return prototype.getName();
    }

    public int getWeight() {
        return weight;
    }

    public int getMinTime() {
        return minTime;
    }

    // Jenis zombie yang boleh muncul di baris air/darat pada detik ke-time
    public static List<ZombieType> available(boolean aquatic, int time) {
        List<ZombieType> result = new ArrayList<>();
        for (ZombieType type : values()) {
            if (type.isAquatic() == aquatic && time >= type.minTime) {
                result.add(type);
            }
        }
        return result;
    }

    // Pilih acak berdasarkan weight
    public static ZombieType random(boolean aquatic, int time, RandomGenerator random) {
        List<ZombieType> candidates = available(aquatic, time);
        int total = 0;
        for (ZombieType type : candidates) {
            total += type.weight;
        }
        int roll = random.nextInt(total);
        for (ZombieType type : candidates) {
            roll -= type.weight;
            if (roll < 0) {
                return type;
            }
        }
        return candidates.get(candidates.size() - 1);
    }
}
