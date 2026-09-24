package entity.zombie;

import entity.DamageType;
import entity.plant.Plant;
import game.Board;
import game.GameEvent;

// Mengendarai lumba-lumba: lebih cepat, dan tanaman pertama yang ditemui langsung mati
public class DolphinRiderZombie extends Zombie {
    private boolean usedInstantKill = false;

    public DolphinRiderZombie(int x, int y) {
        super("Dolphin Rider Zombie", 175, true, 100, 1, x, y, "dolphin");
        walkDelay = 4; // 1 px tiap 5 tick = 12 px/detik
    }

    @Override
    public void update(Board board) {
        if (!usedInstantKill) {
            Plant plant = findTarget(board);
            if (plant != null) {
                plant.takeDamage(plant.getHealth(), DamageType.NORMAL);
                board.fire(GameEvent.PLANT_EATEN);
                usedInstantKill = true;
                return;
            }
        }
        super.update(board);
    }

    @Override
    protected int drawMaxWidth() {
        return 76;
    }

}
