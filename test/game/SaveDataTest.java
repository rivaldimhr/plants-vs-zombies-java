package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import entity.plant.PlantType;

class SaveDataTest {
    @TempDir
    Path dir;

    private SaveData fresh() {
        return new SaveData(dir.resolve("save.properties"));
    }

    @Test
    void newPlayerOnlyHasFirstLevelAndStartingPlants() {
        SaveData save = fresh();
        assertTrue(save.isLevelUnlocked(Level.ADVENTURE.get(0)));
        assertFalse(save.isLevelUnlocked(Level.ADVENTURE.get(1)));
        assertFalse(save.isLevelUnlocked(Level.ENDLESS));
        assertEquals(Level.STARTING_PLANTS.size(), save.getUnlockedPlants().size());
        assertFalse(save.isPlantUnlocked(PlantType.SNOW_PEA));
    }

    @Test
    void completingLevelUnlocksNextLevelAndRewards() {
        SaveData save = fresh();
        List<PlantType> unlocked = save.completeLevel(Level.ADVENTURE.get(0));
        assertEquals(Level.ADVENTURE.get(0).getRewards(), unlocked);
        assertTrue(save.isLevelUnlocked(Level.ADVENTURE.get(1)));
        assertTrue(save.isLevelCompleted(Level.ADVENTURE.get(0)));
        assertTrue(save.isPlantUnlocked(PlantType.SNOW_PEA));
        assertTrue(save.completeLevel(Level.ADVENTURE.get(0)).isEmpty(), "hadiah tidak diberikan dua kali");
    }

    @Test
    void endlessUnlocksAfterLevelThree() {
        SaveData save = fresh();
        for (int i = 0; i < SaveData.ENDLESS_UNLOCK_LEVEL; i++) {
            assertFalse(save.isLevelUnlocked(Level.ENDLESS));
            save.completeLevel(Level.ADVENTURE.get(i));
        }
        assertTrue(save.isLevelUnlocked(Level.ENDLESS));
    }

    @Test
    void progressAndSettingsSurviveReload() {
        SaveData save = fresh();
        save.completeLevel(Level.ADVENTURE.get(0));
        save.completeLevel(Level.ADVENTURE.get(1));
        save.submitEndlessScore(12);
        save.setSoundOn(false);
        save.setWindowScale(1.5);

        SaveData loaded = fresh();
        loaded.load();
        assertEquals(2, loaded.getLevelsCompleted());
        assertEquals(12, loaded.getEndlessBest());
        assertFalse(loaded.isSoundOn());
        assertEquals(1.5, loaded.getWindowScale());
    }

    @Test
    void endlessRecordOnlyGoesUp() {
        SaveData save = fresh();
        assertTrue(save.submitEndlessScore(5));
        assertFalse(save.submitEndlessScore(3));
        assertEquals(5, save.getEndlessBest());
    }

    @Test
    void unlockAllOpensEverything() {
        SaveData save = fresh();
        save.setUnlockAll(true);
        for (Level level : Level.ADVENTURE) {
            assertTrue(save.isLevelUnlocked(level));
        }
        assertEquals(PlantType.values().length, save.getUnlockedPlants().size());
    }

    @Test
    void resetClearsProgress() {
        SaveData save = fresh();
        save.completeLevel(Level.ADVENTURE.get(0));
        save.resetProgress();
        assertEquals(0, save.getLevelsCompleted());
        assertFalse(save.isLevelUnlocked(Level.ADVENTURE.get(1)));
    }

    @Test
    void everyPlantCanBeUnlockedThroughTheAdventure() {
        SaveData save = fresh();
        for (Level level : Level.ADVENTURE) {
            save.completeLevel(level);
        }
        assertEquals(PlantType.values().length, save.getUnlockedPlants().size());
    }
}
