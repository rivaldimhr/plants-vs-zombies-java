package entity.zombie;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import entity.plant.Plant;
import entity.plant.TallNut;
import game.Board;

// Berlari membawa galah dan melompati tanaman pertama yang ditemui (kecuali Tall-nut)
public class PoleVaultingZombie extends Zombie {
    public static final int RUN_DELAY = 5; // lebih cepat selama masih membawa galah
    private static final int JUMP_TICKS = 30;

    private boolean hasPole = true;
    private int jumpTimer = 0; // > 0 selama animasi lompat
    private int jumpFromX;

    public PoleVaultingZombie(int x, int y) {
        super("Pole Vaulting Zombie", 175, false, 100, 1, x, y, "polevault");
        walkDelay = RUN_DELAY;
    }

    public boolean hasPole() {
        return hasPole;
    }

    @Override
    public void update(Board board) {
        if (jumpTimer > 0) {
            jumpTimer--;
            return;
        }
        if (hasPole) {
            Plant plant = findTarget(board);
            if (plant != null) {
                hasPole = false;
                walkDelay = WALK_DELAY;
                if (!(plant instanceof TallNut)) {
                    // mendarat tepat di belakang tanaman yang dilompati
                    jumpFromX = x;
                    x = plant.getX() - TILE / 2;
                    jumpTimer = JUMP_TICKS;
                    timer = 0;
                    return;
                }
                // Tall-nut terlalu tinggi: galah hilang, zombie mulai memakan Tall-nut
            }
        }
        super.update(board);
    }

    // Animasi lompat: melayang dari posisi lama ke posisi baru membentuk busur
    @Override
    protected void applyTransform(Graphics2D g, Rectangle r) {
        if (jumpTimer > 0) {
            double t = 1 - (double) jumpTimer / JUMP_TICKS;
            g.translate((jumpFromX - x) * (1 - t), -50 * Math.sin(Math.PI * t));
            return;
        }
        super.applyTransform(g, r);
    }
}
