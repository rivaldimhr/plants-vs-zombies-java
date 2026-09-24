package entity.plant;

import entity.zombie.Zombie;
import game.Board;

// Tanaman sekali pakai: menyerang semua zombie di dekatnya (depan/belakang) lalu mati
public abstract class InstantKillPlant extends Plant {

    public InstantKillPlant(String name, int health, boolean aquatic, int attackDamage, int cost, int cooldown, int x,
            int y, String img) {
        super(name, health, aquatic, attackDamage, 0, cost, 1, cooldown, x, y, img);
    }

    @Override
    protected void act(Board board) {
        int reach = (range + 1) * Board.TILE_SIZE; // range 1 = tile sendiri + 1 tile di sebelahnya
        boolean hit = false;
        for (Zombie zombie : board.getZombies()) {
            if (zombie.getY() == y && Math.abs(zombie.getX() - x) <= reach) {
                zombie.takeDamage(attackDamage);
                hit = true;
            }
        }
        if (hit) {
            setHealth(0); // mati setelah menyerang
        }
    }
}
