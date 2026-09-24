package game;

import static game.TestUtil.tick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import entity.zombie.Zombie;
import entity.zombie.ZombieType;

class WaveSpawnerTest {

    private static Board board(Level level) {
        return new Board(level, new Random(5));
    }

    // Jalankan board sambil membunuh semua zombie tiap tick (supaya tidak kalah)
    private static void runKilling(Board board, int ticks, List<GameEvent> events) {
        for (int i = 0; i < ticks; i++) {
            board.update();
            for (Zombie zombie : board.getZombies()) {
                zombie.setHealth(0);
            }
        }
    }

    @Test
    void firstWaveComesAfterPreparationTime() {
        Board b = board(Level.ADVENTURE.get(0));
        tick(b, WaveSpawner.FIRST_WAVE - 1);
        assertTrue(b.getZombies().isEmpty());
        assertEquals(0, b.getWaveSpawner().getWavesSpawned());
        tick(b, 2);
        assertFalse(b.getZombies().isEmpty());
        assertEquals(1, b.getWaveSpawner().getWavesSpawned());
    }

    @Test
    void allWavesSpawnThenLevelIsWon() {
        Level level = Level.ADVENTURE.get(0);
        Board b = board(level);
        List<GameEvent> events = new ArrayList<>();
        b.addListener(events::add);
        runKilling(b, 5 * 60 * Game.UPS, events);
        assertTrue(b.getWaveSpawner().isFinished());
        assertEquals(level.getWaveCount(), b.getWaveSpawner().getWavesSpawned());
        assertEquals(Board.Result.WON, b.getResult());
        assertTrue(events.contains(GameEvent.HUGE_WAVE), "ada peringatan huge wave");
        assertTrue(events.contains(GameEvent.FINAL_WAVE), "ada final wave");
        assertTrue(events.contains(GameEvent.LEVEL_WON));
    }

    @Test
    void nextWaveComesEarlyWhenPreviousWaveIsDefeated() {
        Board b = board(Level.ADVENTURE.get(2));
        tick(b, WaveSpawner.FIRST_WAVE + 1);
        assertEquals(1, b.getWaveSpawner().getWavesSpawned());
        for (Zombie zombie : b.getZombies()) {
            zombie.setHealth(0);
        }
        tick(b, WaveSpawner.MIN_WAVE_GAP + 5);
        assertEquals(2, b.getWaveSpawner().getWavesSpawned(), "tidak menunggu 25 detik penuh");
    }

    @Test
    void zombiesSpawnInMatchingRows() {
        for (Level level : Level.ADVENTURE) {
            Board b = board(level);
            for (int w = 0; w < level.getWaveCount(); w++) {
                tick(b, WaveSpawner.WAVE_INTERVAL + WaveSpawner.FIRST_WAVE);
            }
            for (Zombie zombie : b.getZombies()) {
                assertEquals(Board.isWaterRow(zombie.getRow()), zombie.isAquatic(), zombie.getName());
            }
        }
    }

    @Test
    void wavesAreDeterministicAndUseOnlyLevelZombies() {
        for (Level level : Level.ADVENTURE) {
            for (int i = 0; i < level.getWaveCount(); i++) {
                Wave wave = level.getWave(i);
                assertFalse(wave.zombies().isEmpty());
                assertEquals(level.isFlagWave(i), wave.flag());
                for (ZombieType type : wave.zombies()) {
                    assertTrue(level.getZombieTypes().contains(type), level.getTitle() + ": " + type);
                }
            }
            assertEquals(level.getWave(0), level.getWave(0));
        }
    }

    @Test
    void flagWavesAreBiggerThanNormalWaves() {
        Level level = Level.ADVENTURE.get(1);
        int flagPoints = points(level.getWave(3)); // wave 4 = bendera
        int normalPoints = points(level.getWave(2));
        assertTrue(flagPoints > normalPoints * 2, flagPoints + " vs " + normalPoints);
    }

    @Test
    void strongZombiesDoNotAppearInFirstWaves() {
        Level level = Level.ADVENTURE.get(5);
        for (ZombieType type : level.getWave(0).zombies()) {
            assertTrue(type.getPoints() <= 2, type.toString());
        }
    }

    @Test
    void levelOneHasNoPoolZombies() {
        assertFalse(Level.ADVENTURE.get(0).hasAquaticZombies());
        assertTrue(Level.ADVENTURE.get(1).hasAquaticZombies());
    }

    @Test
    void endlessKeepsGeneratingWavesWithFlagsEveryTen() {
        Level endless = Level.ENDLESS;
        assertTrue(endless.isEndless());
        assertFalse(endless.getWave(40).zombies().isEmpty());
        assertTrue(endless.isFlagWave(9));
        assertFalse(endless.isFlagWave(10));
        assertTrue(points(endless.getWave(30)) > points(endless.getWave(1)));
    }

    private static int points(Wave wave) {
        return wave.zombies().stream().mapToInt(ZombieType::getPoints).sum();
    }
}
