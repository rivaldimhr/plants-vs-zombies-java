package entity.plant;

import game.Board;
import game.Game;

// Tanaman yang menembak tiap attackSpeed detik selama ada zombie dalam range
public abstract class ShooterPlant extends Plant {

    public ShooterPlant(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int cost,
            int range, int cooldown, int x, int y, String img) {
        super(name, health, aquatic, attackDamage, attackSpeed, cost, range, cooldown, x, y, img);
    }

    protected abstract void shoot(Board board);

    @Override
    protected void act(Board board) {
        if (timer < attackSpeed * Game.UPS) {
            timer++;
        } else if (zombieInRange(board)) {
            shoot(board);
            timer = 0;
        }
    }
}
