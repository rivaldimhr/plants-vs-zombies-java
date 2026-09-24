package game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import entity.plant.PlantType;

/**
 * Progres dan pengaturan pemain, disimpan di ~/.pvz-java/save.properties:
 * level yang sudah selesai, skor terbaik Endless, suara, musik, ukuran jendela.
 */
public class SaveData {
    public static final int ENDLESS_UNLOCK_LEVEL = 3; // Endless terbuka setelah level 3 selesai

    private final Path file;
    private int levelsCompleted = 0;
    private int endlessBest = 0;
    private boolean soundOn = true;
    private boolean musicOn = true;
    private double windowScale = 1.0;
    private boolean unlockAll = false;

    public SaveData(Path file) {
        this.file = file;
    }

    public static Path defaultFile() {
        return Paths.get(System.getProperty("user.home"), ".pvz-java", "save.properties");
    }

    public static SaveData loadDefault() {
        SaveData data = new SaveData(defaultFile());
        data.load();
        return data;
    }

    public void load() {
        if (!Files.isRegularFile(file)) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            p.load(in);
            levelsCompleted = clamp(Integer.parseInt(p.getProperty("levelsCompleted", "0")), 0, Level.ADVENTURE.size());
            endlessBest = Math.max(0, Integer.parseInt(p.getProperty("endlessBest", "0")));
            soundOn = Boolean.parseBoolean(p.getProperty("sound", "true"));
            musicOn = Boolean.parseBoolean(p.getProperty("music", "true"));
            windowScale = Double.parseDouble(p.getProperty("windowScale", "1.0"));
            unlockAll = Boolean.parseBoolean(p.getProperty("unlockAll", "false"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Save rusak, memakai data baru: " + e.getMessage());
        }
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("levelsCompleted", Integer.toString(levelsCompleted));
        p.setProperty("endlessBest", Integer.toString(endlessBest));
        p.setProperty("sound", Boolean.toString(soundOn));
        p.setProperty("music", Boolean.toString(musicOn));
        p.setProperty("windowScale", Double.toString(windowScale));
        p.setProperty("unlockAll", Boolean.toString(unlockAll));
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                p.store(out, "Plants vs Zombies (Java) - progres pemain");
            }
        } catch (IOException e) {
            System.err.println("Gagal menyimpan progres: " + e.getMessage());
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    // ------------------------------------------------------------------ progres

    public boolean isLevelUnlocked(Level level) {
        if (level.isEndless()) {
            return unlockAll || levelsCompleted >= ENDLESS_UNLOCK_LEVEL;
        }
        return unlockAll || level.getIndex() <= levelsCompleted;
    }

    public boolean isLevelCompleted(Level level) {
        return !level.isEndless() && level.getIndex() < levelsCompleted;
    }

    // Return tanaman yang baru terbuka karena level ini (kosong kalau sudah pernah selesai)
    public List<PlantType> completeLevel(Level level) {
        List<PlantType> unlocked = new ArrayList<>();
        if (level.isEndless() || level.getIndex() < levelsCompleted) {
            return unlocked;
        }
        levelsCompleted = level.getIndex() + 1;
        unlocked.addAll(level.getRewards());
        save();
        return unlocked;
    }

    public boolean isPlantUnlocked(PlantType type) {
        if (unlockAll || Level.STARTING_PLANTS.contains(type)) {
            return true;
        }
        for (int i = 0; i < levelsCompleted; i++) {
            if (Level.ADVENTURE.get(i).getRewards().contains(type)) {
                return true;
            }
        }
        return false;
    }

    public List<PlantType> getUnlockedPlants() {
        List<PlantType> result = new ArrayList<>();
        for (PlantType type : PlantType.values()) {
            if (isPlantUnlocked(type)) {
                result.add(type);
            }
        }
        return result;
    }

    // Return true kalau ini rekor baru
    public boolean submitEndlessScore(int waves) {
        if (waves > endlessBest) {
            endlessBest = waves;
            save();
            return true;
        }
        return false;
    }

    public void resetProgress() {
        levelsCompleted = 0;
        endlessBest = 0;
        save();
    }

    // ------------------------------------------------------------------ getter & setter

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public int getEndlessBest() {
        return endlessBest;
    }

    public boolean isSoundOn() {
        return soundOn;
    }

    public void setSoundOn(boolean soundOn) {
        this.soundOn = soundOn;
        save();
    }

    public boolean isMusicOn() {
        return musicOn;
    }

    public void setMusicOn(boolean musicOn) {
        this.musicOn = musicOn;
        save();
    }

    public double getWindowScale() {
        return windowScale;
    }

    public void setWindowScale(double windowScale) {
        this.windowScale = windowScale;
        save();
    }

    public boolean isUnlockAll() {
        return unlockAll;
    }

    public void setUnlockAll(boolean unlockAll) {
        this.unlockAll = unlockAll;
        save();
    }
}
