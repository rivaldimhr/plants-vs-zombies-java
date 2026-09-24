package entity;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import game.Assets;
import game.Board;
import game.Game;

/**
 * Sun yang bisa diklik. Jatuh dari langit (siang hari) atau keluar dari
 * Sunflower / Sun-shroom. Kalau tidak diambil, hilang setelah beberapa detik.
 * Saat diklik, nilainya langsung masuk ke bank sun dan gambarnya terbang ke
 * penghitung sun di pojok kiri atas.
 */
public class SunToken implements Updatable {
    public static final int LIFETIME = 10 * Game.UPS; // lama sun menunggu diambil
    public static final double FALL_SPEED = 1.0; // px per tick
    private static final int COLLECT_TICKS = 28;
    private static final double BANK_X = 26, BANK_Y = 22; // posisi ikon sun di seed bank

    public enum State {
        FALLING, RESTING, COLLECTING, DONE
    }

    private final int value;
    private double x, y; // titik tengah
    private double vx, vy;
    private final double groundY;
    private final boolean gravity; // sun dari tanaman melompat (pakai gravitasi), sun langit jatuh lurus
    private State state = State.FALLING;
    private int age = 0;
    private int restTime = 0;
    private int collectTime = 0;
    private double collectStartX, collectStartY;

    private SunToken(int value, double x, double y, double vx, double vy, double groundY, boolean gravity) {
        this.value = value;
        this.gravity = gravity;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.groundY = groundY;
    }

    // Sun dari langit: jatuh lurus sampai groundY
    public static SunToken fromSky(double x, double groundY, int value) {
        return new SunToken(value, x, -30, 0, FALL_SPEED, groundY, false);
    }

    // Sun dari tanaman: melompat kecil ke samping lalu mendarat
    public static SunToken fromPlant(double x, double y, int value, double dx) {
        return new SunToken(value, x, y, dx, -3.2, y + 12, true);
    }

    @Override
    public void update(Board board) {
        age++;
        switch (state) {
            case FALLING:
                x += vx;
                y += vy;
                if (gravity) {
                    vy += 0.25;
                }
                if (y >= groundY && vy > 0) {
                    y = groundY;
                    state = State.RESTING;
                }
                break;
            case RESTING:
                if (++restTime >= LIFETIME) {
                    state = State.DONE;
                }
                break;
            case COLLECTING:
                collectTime++;
                double t = Math.min(1, collectTime / (double) COLLECT_TICKS);
                double ease = 1 - (1 - t) * (1 - t);
                x = collectStartX + (BANK_X - collectStartX) * ease;
                y = collectStartY + (BANK_Y - collectStartY) * ease;
                if (collectTime >= COLLECT_TICKS) {
                    state = State.DONE;
                }
                break;
            default:
                break;
        }
    }

    public boolean isCollectable() {
        return state == State.FALLING || state == State.RESTING;
    }

    public boolean contains(double px, double py) {
        double r = size() / 2.0 + 6;
        return isCollectable() && (px - x) * (px - x) + (py - y) * (py - y) <= r * r;
    }

    public void collect() {
        if (isCollectable()) {
            state = State.COLLECTING;
            collectStartX = x;
            collectStartY = y;
        }
    }

    public boolean isDone() {
        return state == State.DONE;
    }

    public State getState() {
        return state;
    }

    public int getValue() {
        return value;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    private int size() {
        return value >= 25 ? 44 : 30;
    }

    public void draw(Graphics2D g) {
        BufferedImage frame = Assets.sprite("sun").frameAt(age * 1000L / Game.UPS, true);
        int s = size();
        float alpha = 1f;
        if (state == State.RESTING && restTime > LIFETIME - 2 * Game.UPS) {
            alpha = (restTime / 6) % 2 == 0 ? 0.35f : 0.9f; // berkedip sebelum hilang
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.rotate(age * 0.02, x, y);
        g2.drawImage(frame, (int) (x - s / 2.0), (int) (y - s / 2.0), s, s, null);
        g2.dispose();
    }
}
