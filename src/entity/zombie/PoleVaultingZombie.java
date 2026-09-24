package entity.zombie;

import entity.plant.Plant;
import entity.plant.TallNut;
import game.Board;

// Berlari membawa galah dan melompati tanaman pertama yang ditemui (kecuali Tall-nut)
public class PoleVaultingZombie extends Zombie {
    public static final int RUN_DELAY = 5; // lebih cepat selama masih membawa galah
    private boolean hasPole = true;

    public PoleVaultingZombie(int x, int y) {
        super("Pole Vaulting Zombie", 175, false, 100, 1, x, y, "image/sprites/Polerun.gif");
        walkDelay = RUN_DELAY;
    }

    public boolean hasPole() {
        return hasPole;
    }

    @Override
    public void update(Board board) {
        if (hasPole) {
            Plant plant = findTarget(board);
            if (plant != null) {
                hasPole = false;
                walkDelay = WALK_DELAY;
                if (!(plant instanceof TallNut)) {
                    // mendarat tepat di belakang tanaman yang dilompati
                    x = plant.getX() - Board.TILE_SIZE / 2;
                    timer = 0;
                    return;
                }
                // Tall-nut terlalu tinggi: galah hilang, zombie mulai memakan Tall-nut
            }
        }
        super.update(board);
    }
}
