package entity;

// Jenis serangan; menentukan animasi mati zombie
public enum DamageType {
    NORMAL, // peluru, gigitan
    EXPLOSION, // Cherry Bomb, Potato Mine: zombie gosong
    FIRE, // Jalapeno: zombie gosong
    CRUSH, // Squash: zombie gepeng
    DROWN, // Tangle Kelp: zombie tenggelam
    MOWER; // lawn mower: zombie terlempar

    public boolean burns() {
        return this == EXPLOSION || this == FIRE;
    }
}
