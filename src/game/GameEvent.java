package game;

/**
 * Kejadian dalam permainan yang bisa didengar pihak lain (pola Observer).
 * Board tidak tahu soal suara; SoundManager yang mendengarkan event ini.
 */
public enum GameEvent {
    PLANT,
    DIG,
    SHOOT,
    HIT,
    CHOMP,
    PLANT_EATEN,
    ZOMBIE_DIED,
    ARMOR_LOST,
    EXPLOSION,
    MINE_EXPLOSION,
    FIRE,
    SUN_COLLECTED,
    LAWN_MOWER,
    GROAN,
    WAVE,
    HUGE_WAVE,
    FINAL_WAVE,
    LEVEL_WON,
    LEVEL_LOST;
}
