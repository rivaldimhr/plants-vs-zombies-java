package game;

// Pendengar event permainan (misalnya SoundManager)
@FunctionalInterface
public interface GameEventListener {
    void onEvent(GameEvent event);
}
