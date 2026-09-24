package entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import entity.zombie.Zombie;
import game.Assets;
import game.Board;
import game.Game;
import game.GameEvent;

/**
 * Mesin pemotong rumput di ujung kiri tiap baris: pertahanan terakhir.
 * Saat zombie mencapai rumah, mesin menyala, melaju ke kanan dan menghabisi
 * semua zombie di baris itu. Setiap mesin hanya bisa dipakai sekali.
 */
public class LawnMower implements Updatable {
    public static final int TRIGGER_X = 30; // zombie di kiri garis ini menyalakan mesin
    public static final int SPEED = 5; // px per tick

    public enum State {
        IDLE, RUNNING, GONE
    }

    private final int row;
    private double x = -8;
    private State state = State.IDLE;
    private int age = 0;

    public LawnMower(int row) {
        this.row = row;
    }

    @Override
    public void update(Board board) {
        age++;
        if (state == State.IDLE) {
            for (Zombie zombie : board.getZombies()) {
                if (zombie.getRow() == row && !zombie.isDead() && zombie.getX() <= TRIGGER_X) {
                    state = State.RUNNING;
                    board.fire(GameEvent.LAWN_MOWER);
                    break;
                }
            }
        }
        if (state == State.RUNNING) {
            x += SPEED;
            for (Zombie zombie : board.getZombies()) {
                if (zombie.getRow() == row && !zombie.isDead() && zombie.getX() <= x + 40) {
                    zombie.kill(DamageType.MOWER);
                }
            }
            if (x > Board.WIDTH) {
                state = State.GONE;
            }
        }
    }

    public int getRow() {
        return row;
    }

    public State getState() {
        return state;
    }

    public double getX() {
        return x;
    }

    public void draw(Graphics2D g) {
        if (state == State.GONE) {
            return;
        }
        long time = state == State.RUNNING ? age * 1000L / Game.UPS : 0;
        BufferedImage frame = Assets.sprite("lawnmower").frameAt(time, true);
        int w = 50;
        int h = frame.getHeight() * w / frame.getWidth();
        int top = (row + 1) * Board.TILE_SIZE - h - 4;
        int shake = state == State.RUNNING ? (age % 4 < 2 ? -1 : 1) : 0;
        g.drawImage(frame, (int) x, top + shake, w, h, null);
    }
}
