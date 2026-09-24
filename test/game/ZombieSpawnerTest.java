package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import entity.zombie.Zombie;
import entity.zombie.ZombieType;

class ZombieSpawnerTest {

    // Board tanpa update entity: hanya memanggil spawner tiap detik
    private static Board spawnOnly(long seed, int fromSecond, int toSecond) {
        Board board = new Board(new Random(seed));
        ZombieSpawner spawner = new ZombieSpawner(new Random(seed));
        for (int t = fromSecond; t < toSecond; t++) {
            board.setTime(t);
            spawner.onSecond(board);
        }
        return board;
    }

    @Test
    void noZombiesBeforeSpawnStart() {
        Board board = spawnOnly(1, 0, ZombieSpawner.SPAWN_START);
        assertTrue(board.getZombies().isEmpty());
    }

    @Test
    void noZombiesAfterGameDuration() {
        Board board = spawnOnly(1, Board.GAME_DURATION, Board.GAME_DURATION + 60);
        assertTrue(board.getZombies().isEmpty());
    }

    @Test
    void zombiesAppearAndRespectMaximum() {
        Board board = spawnOnly(7, ZombieSpawner.SPAWN_START, ZombieSpawner.HUGE_WAVE_START);
        assertEquals(ZombieSpawner.MAX_ZOMBIES, board.getZombies().size());
    }

    @Test
    void aquaticZombiesOnlyInPoolRows() {
        for (long seed = 0; seed < 20; seed++) {
            Board board = spawnOnly(seed, ZombieSpawner.SPAWN_START, ZombieSpawner.HUGE_WAVE_START);
            for (Zombie zombie : board.getZombies()) {
                assertEquals(Board.isWaterRow(zombie.getRow()), zombie.isAquatic(), zombie.getName());
                assertEquals(Board.COLS - 1, zombie.getCol());
            }
        }
    }

    @Test
    void hugeWaveSpawnsOneZombiePerRow() {
        Board board = new Board(new Random(3));
        board.setTime(ZombieSpawner.HUGE_WAVE_START);
        new ZombieSpawner(new Random(3)).onSecond(board);
        assertEquals(Board.ROWS - 1, board.getZombies().size());
        Set<Integer> rows = new java.util.HashSet<>();
        for (Zombie zombie : board.getZombies()) {
            rows.add(zombie.getRow());
        }
        assertEquals(Board.ROWS - 1, rows.size());
    }

    @Test
    void strongZombiesOnlyAfterTheirMinimumTime() {
        assertFalse(ZombieType.available(false, 20).contains(ZombieType.FOOTBALL));
        assertTrue(ZombieType.available(false, ZombieType.FOOTBALL.getMinTime()).contains(ZombieType.FOOTBALL));
        assertFalse(ZombieType.available(true, 20).contains(ZombieType.DOLPHIN_RIDER));
    }

    @Test
    void everyZombieTypeCanEventuallySpawn() {
        Random random = new Random(11);
        Set<ZombieType> seen = EnumSet.noneOf(ZombieType.class);
        for (int i = 0; i < 2000; i++) {
            seen.add(ZombieType.random(i % 2 == 0, 200, random));
        }
        assertEquals(EnumSet.allOf(ZombieType.class), seen);
    }
}
