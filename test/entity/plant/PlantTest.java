package entity.plant;

import static game.TestUtil.px;
import static game.TestUtil.quietBoard;
import static game.TestUtil.seconds;
import static game.TestUtil.tick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import entity.projectile.Bullet;
import entity.projectile.PuffBullet;
import entity.zombie.BucketheadZombie;
import entity.zombie.NormalZombie;
import entity.zombie.Zombie;
import game.Board;
import game.Game;

class PlantTest {

    // Zombie target yang tidak bergerak & tidak menyerang
    private static Zombie dummy(int col, int row) {
        return new NormalZombie(px(col), px(row)) {
            @Override
            public void update(Board board) {
            }
        };
    }

    @Test
    void peashooterDoesNotShootWithoutZombie() {
        Board board = quietBoard();
        board.addPlant(new Peashooter(px(2), px(2)));
        seconds(board, 10);
        assertTrue(board.getBullets().isEmpty());
    }

    @Test
    void peashooterShootsEveryAttackSpeedSeconds() {
        Board board = quietBoard();
        Peashooter peashooter = new Peashooter(px(1), px(2));
        board.addPlant(peashooter);
        board.addZombie(dummy(9, 2));
        int period = (int) (peashooter.getAttackSpeed() * Game.UPS) + 1;
        tick(board, period - 1);
        assertEquals(0, board.getBullets().size());
        tick(board, 1);
        assertEquals(1, board.getBullets().size());
    }

    @Test
    void peashooterIgnoresZombieInOtherRowOrBehind() {
        Board board = quietBoard();
        board.addPlant(new Peashooter(px(5), px(2)));
        board.addZombie(dummy(8, 1));
        board.addZombie(dummy(3, 2));
        seconds(board, 10);
        assertTrue(board.getBullets().isEmpty());
    }

    @Test
    void peaBulletDamagesZombie() {
        Board board = quietBoard();
        board.addPlant(new Peashooter(px(1), px(2)));
        Zombie zombie = dummy(6, 2);
        board.addZombie(zombie);
        seconds(board, 8);
        assertTrue(zombie.getHealth() < zombie.getMaxHealth());
        assertEquals(0, (zombie.getMaxHealth() - zombie.getHealth()) % 25);
    }

    @Test
    void repeaterShootsTwoPeas() {
        Board board = quietBoard();
        Repeater repeater = new Repeater(px(1), px(2));
        board.addPlant(repeater);
        board.addZombie(dummy(9, 2));
        tick(board, (int) (repeater.getAttackSpeed() * Game.UPS) + 1);
        assertEquals(2, board.getBullets().size());
    }

    @Test
    void snowPeaSlowsZombie() {
        Board board = quietBoard();
        board.addPlant(new SnowPea(px(1), px(2)));
        Zombie zombie = dummy(5, 2);
        board.addZombie(zombie);
        boolean slowed = false;
        for (int i = 0; i < 10 * Game.UPS && !slowed; i++) {
            board.update();
            slowed = zombie.isSlowed();
        }
        assertTrue(slowed);
    }

    @Test
    void puffShroomSleepsDuringDayAndShootsAtNight() {
        Board board = quietBoard();
        PuffShroom puff = new PuffShroom(px(2), px(2));
        board.addPlant(puff);
        Zombie zombie = dummy(4, 2);
        board.addZombie(zombie);
        seconds(board, 6);
        assertTrue(puff.isAsleep());
        assertTrue(board.getBullets().isEmpty());
        assertEquals(zombie.getMaxHealth(), zombie.getHealth());

        board.setTime(Board.DAY_LENGTH); // malam
        seconds(board, 6);
        assertFalse(puff.isAsleep());
        assertEquals(zombie.getMaxHealth() - puff.getAttackDamage(), zombie.getHealth());
    }

    @Test
    void puffShroomHasShortRange() {
        Board board = quietBoard();
        board.setTime(Board.DAY_LENGTH); // malam
        board.addPlant(new PuffShroom(px(1), px(2)));
        board.addZombie(dummy(1 + PuffShroom.RANGE + 2, 2));
        seconds(board, 10);
        assertTrue(board.getBullets().isEmpty());

        PuffBullet bullet = new PuffBullet(px(1), px(5), 15);
        board.addBullet(bullet);
        seconds(board, 5);
        assertTrue(bullet.isDone());
        assertTrue(bullet.getX() < Board.WIDTH);
    }

    @Test
    void sunflowerProducesSun() {
        Board board = quietBoard();
        board.setTime(Board.DAY_LENGTH); // malam, supaya tidak ada sun jatuh
        board.addPlant(new Sunflower(px(2), px(2)));
        int before = board.getSun().get();
        seconds(board, Sunflower.SUN_INTERVAL * 2 + 0.5);
        assertEquals(before + 2 * Sunflower.SUN_AMOUNT, board.getSun().get());
    }

    @Test
    void squashKillsNearbyZombiesAndDies() {
        Board board = quietBoard();
        Squash squash = new Squash(px(4), px(2));
        board.addPlant(squash);
        Zombie near = new BucketheadZombie(px(5), px(2));
        Zombie far = new BucketheadZombie(px(8), px(2));
        board.addZombie(near);
        board.addZombie(far);
        board.update();
        assertTrue(near.isDead());
        assertFalse(far.isDead());
        assertTrue(board.getPlants().isEmpty());
    }

    @Test
    void wallNutImageCracksAsHealthDrops() {
        WallNut nut = new WallNut(px(2), px(2));
        String full = nut.getImagePath();
        nut.takeDamage(400);
        String cracked = nut.getImagePath();
        nut.takeDamage(400);
        String veryCracked = nut.getImagePath();
        assertNotEquals(full, cracked);
        assertNotEquals(cracked, veryCracked);
    }

    @Test
    void bulletLeavingScreenIsRemoved() {
        Board board = quietBoard();
        board.addBullet(new entity.projectile.PeaBullet(px(9), px(2), 25));
        seconds(board, 2);
        assertTrue(board.getBullets().isEmpty());
    }

    @Test
    void onlyOneZombieHitPerBullet() {
        Board board = quietBoard();
        Zombie a = dummy(5, 2);
        Zombie b = dummy(5, 2);
        board.addZombie(a);
        board.addZombie(b);
        Bullet bullet = new entity.projectile.PeaBullet(px(4), px(2), 25);
        board.addBullet(bullet);
        seconds(board, 2);
        assertEquals(25, (a.getMaxHealth() - a.getHealth()) + (b.getMaxHealth() - b.getHealth()));
    }
}
