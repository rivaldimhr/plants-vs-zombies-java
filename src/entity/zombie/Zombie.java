package entity.zombie;

import java.awt.Color;
import java.awt.Graphics2D;

import entity.Entity;
import entity.plant.LilyPad;
import entity.plant.Plant;
import game.Board;
import game.Game;

public abstract class Zombie extends Entity {
    public static final int WALK_DELAY = 10; // jalan 1 pixel tiap (WALK_DELAY + 1) tick ≈ 5,5 px/detik
    public static final int SLOW_DURATION = 3 * Game.UPS;
    private static final int CONTACT_RANGE = 90; // jarak (px) zombie mulai memakan tanaman

    protected int walkDelay = WALK_DELAY;
    protected Plant target;
    private boolean slowed = false;
    private int slowTime = 0;

    public Zombie(String name, int health, boolean aquatic, int attackDamage, double attackSpeed, int x, int y,
            String img) {
        super(name, health, aquatic, attackDamage, attackSpeed, x, y, img);
    }

    // Dipanggil SlowBullet: kecepatan jalan & serang jadi setengah selama 3 detik
    public void applySlow() {
        slowed = true;
        slowTime = 0;
    }

    public boolean isSlowed() {
        return slowed;
    }

    // Bisa kena peluru atau tidak (Snorkel Zombie tidak bisa saat menyelam)
    public boolean isTargetable() {
        return true;
    }

    public boolean isEating() {
        return target != null;
    }

    protected void move() {
        x--;
    }

    // Tanaman di depan zombie (baris sama, dalam jarak kontak). Tanaman di atas Lily Pad didahulukan.
    protected Plant findTarget(Board board) {
        Plant found = null;
        for (Plant plant : board.getPlants()) {
            if (plant.getY() == y && x >= plant.getX() && x - CONTACT_RANGE <= plant.getX()) {
                if (found == null || found instanceof LilyPad) {
                    found = plant;
                }
            }
        }
        return found;
    }

    @Override
    public void update(Board board) {
        if (slowed && ++slowTime >= SLOW_DURATION) {
            slowed = false;
        }
        int factor = slowed ? 2 : 1;

        target = findTarget(board);
        if (target == null) {
            if (timer >= walkDelay * factor) {
                move();
                timer = 0;
            } else {
                timer++;
            }
        } else {
            if (timer >= attackSpeed * Game.UPS * factor) {
                target.takeDamage(attackDamage);
                timer = 0;
            } else {
                timer++;
            }
        }
    }

    @Override
    public void draw(Graphics2D g) {
        drawSprite(g, isTargetable() ? 1f : 0.4f);
        if (slowed) {
            // tanda sedang diperlambat
            g.setColor(new Color(120, 200, 255, 200));
            g.fillOval(x + 6, y + 8, 10, 10);
            g.setColor(Color.WHITE);
            g.drawOval(x + 6, y + 8, 10, 10);
        }
    }

    public int getWalkDelay() {
        return walkDelay;
    }

}
