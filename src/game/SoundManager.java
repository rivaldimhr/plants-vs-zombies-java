package game;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/**
 * Memutar efek suara dan musik (file WAV di folder sound/, dibuat oleh
 * tools/generate_sounds.py). Mendengarkan GameEvent dari Board (pola Observer),
 * jadi logika permainan tidak perlu tahu soal suara.
 * Kalau perangkat audio tidak tersedia, semua suara diam tanpa error.
 */
public class SoundManager implements GameEventListener {
    private static final int VOICES = 4; // suara yang sama bisa diputar bertumpuk
    private static final Map<GameEvent, String> EVENT_SOUNDS = new EnumMap<>(GameEvent.class);
    private static final Map<String, Integer> MIN_GAP_MS = new HashMap<>();

    static {
        EVENT_SOUNDS.put(GameEvent.PLANT, "plant");
        EVENT_SOUNDS.put(GameEvent.DIG, "dig");
        EVENT_SOUNDS.put(GameEvent.SHOOT, "shoot");
        EVENT_SOUNDS.put(GameEvent.HIT, "hit");
        EVENT_SOUNDS.put(GameEvent.CHOMP, "chomp");
        EVENT_SOUNDS.put(GameEvent.PLANT_EATEN, "gulp");
        EVENT_SOUNDS.put(GameEvent.ARMOR_LOST, "hit");
        EVENT_SOUNDS.put(GameEvent.EXPLOSION, "explode");
        EVENT_SOUNDS.put(GameEvent.MINE_EXPLOSION, "mine");
        EVENT_SOUNDS.put(GameEvent.FIRE, "fire");
        EVENT_SOUNDS.put(GameEvent.SUN_COLLECTED, "sun");
        EVENT_SOUNDS.put(GameEvent.LAWN_MOWER, "mower");
        EVENT_SOUNDS.put(GameEvent.GROAN, "groan");
        EVENT_SOUNDS.put(GameEvent.WAVE, "groan");
        EVENT_SOUNDS.put(GameEvent.HUGE_WAVE, "siren");
        EVENT_SOUNDS.put(GameEvent.FINAL_WAVE, "finalwave");
        EVENT_SOUNDS.put(GameEvent.LEVEL_WON, "win");
        EVENT_SOUNDS.put(GameEvent.LEVEL_LOST, "lose");
        MIN_GAP_MS.put("shoot", 60);
        MIN_GAP_MS.put("hit", 50);
        MIN_GAP_MS.put("chomp", 120);
        MIN_GAP_MS.put("groan", 1500);
    }

    private final Map<String, Clip[]> clips = new HashMap<>();
    private final Map<String, Integer> nextVoice = new HashMap<>();
    private final Map<String, Long> lastPlayed = new HashMap<>();
    private final SaveData settings;
    private Clip music;
    private String musicName;
    private boolean available = true;

    public SoundManager(SaveData settings) {
        this.settings = settings;
    }

    @Override
    public void onEvent(GameEvent event) {
        String name = EVENT_SOUNDS.get(event);
        if (name != null) {
            play(name);
        }
    }

    public synchronized void play(String name) {
        if (!settings.isSoundOn() || !available) {
            return;
        }
        long now = System.currentTimeMillis();
        Long last = lastPlayed.get(name);
        if (last != null && now - last < MIN_GAP_MS.getOrDefault(name, 30)) {
            return;
        }
        lastPlayed.put(name, now);
        Clip[] voices = clips.computeIfAbsent(name, n -> loadVoices(n, VOICES));
        if (voices == null || voices.length == 0) {
            return;
        }
        int index = nextVoice.merge(name, 1, Integer::sum) % voices.length;
        Clip clip = voices[index];
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    // Putar musik berulang (misalnya "music_day"); tidak mengulang kalau sudah diputar
    public synchronized void playMusic(String name) {
        if (name.equals(musicName) && music != null && music.isRunning()) {
            return;
        }
        stopMusic();
        musicName = name;
        if (!settings.isMusicOn() || !available) {
            return;
        }
        Clip[] loaded = loadVoices(name, 1);
        if (loaded != null && loaded.length > 0) {
            music = loaded[0];
            music.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public synchronized void stopMusic() {
        if (music != null) {
            music.stop();
            music.close();
            music = null;
        }
    }

    // Dipanggil setelah pengaturan musik diubah
    public synchronized void refreshMusic() {
        String current = musicName;
        stopMusic();
        musicName = null;
        if (current != null && settings.isMusicOn()) {
            playMusic(current);
        } else {
            musicName = current;
        }
    }

    private Clip[] loadVoices(String name, int count) {
        URL url = Assets.resource("sound/" + name + ".wav");
        if (url == null) {
            Assets.warnMissing("sound/" + name + ".wav");
            return new Clip[0];
        }
        try {
            Clip[] result = new Clip[count];
            for (int i = 0; i < count; i++) {
                try (InputStream raw = url.openStream();
                        AudioInputStream audio = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                    Clip clip = AudioSystem.getClip();
                    clip.open(audio);
                    result[i] = clip;
                }
            }
            return result;
        } catch (Exception | LinkageError e) {
            // tidak ada perangkat audio (misalnya server tanpa sound card): diam saja
            available = false;
            System.err.println("Suara dimatikan: " + e.getMessage());
            return new Clip[0];
        }
    }
}
