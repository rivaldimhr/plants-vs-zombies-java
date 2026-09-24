package entity.zombie;

// Berlari dengan perlengkapan rugby: HP tinggi dan 2x lebih cepat
public class FootballZombie extends Zombie {

    public FootballZombie(int x, int y) {
        super("Football Zombie", 300, false, 100, 1, x, y, "image/sprites/Running_Zombie_football.gif");
        walkDelay = WALK_DELAY / 2;
    }

}
