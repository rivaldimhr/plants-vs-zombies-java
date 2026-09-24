package entity.plant;

import static game.TestUtil.nightBoard;
import static game.TestUtil.px;
import static game.TestUtil.quietBoard;
import static game.TestUtil.seconds;
import static game.TestUtil.tick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import entity.DamageType;
import entity.SunToken;
import entity.projectile.Bullet;
import entity.projectile.PeaBullet;
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

    // ------------------------------------------------------------------ penembak

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
        seconds(board, 6);
        assertEquals(zombie.getMaxHealth() - 25, zombie.getHealth());
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
    void onlyOneZombieHitPerBullet() {
        Board board = quietBoard();
        Zombie a = dummy(5, 2);
        Zombie b = dummy(5, 2);
        board.addZombie(a);
        board.addZombie(b);
        Bullet bullet = new PeaBullet(px(4), px(2), 25);
        board.addBullet(bullet);
        seconds(board, 2);
        assertEquals(25, (a.getMaxHealth() - a.getHealth()) + (b.getMaxHealth() - b.getHealth()));
    }

    @Test
    void bulletLeavingScreenIsRemoved() {
        Board board = quietBoard();
        board.addBullet(new PeaBullet(px(9), px(2), 25));
        seconds(board, 2);
        assertTrue(board.getBullets().isEmpty());
    }

    // ------------------------------------------------------------------ jamur (malam)

    @Test
    void puffShroomSleepsOnDayLevels() {
        Board board = quietBoard();
        PuffShroom puff = new PuffShroom(px(2), px(2));
        board.addPlant(puff);
        Zombie zombie = dummy(4, 2);
        board.addZombie(zombie);
        seconds(board, 6);
        assertTrue(puff.isAsleep());
        assertEquals(zombie.getMaxHealth(), zombie.getHealth());
    }

    @Test
    void puffShroomShootsAtNightWithShortRange() {
        Board board = nightBoard();
        PuffShroom puff = new PuffShroom(px(2), px(2));
        board.addPlant(puff);
        Zombie near = dummy(4, 2);
        Zombie far = dummy(2 + PuffShroom.RANGE + 2, 5);
        board.addZombie(near);
        board.addZombie(far);
        board.addPlant(new PuffShroom(px(1), px(5)));
        seconds(board, 6);
        assertFalse(puff.isAsleep());
        assertEquals(near.getMaxHealth() - puff.getAttackDamage(), near.getHealth());
        assertEquals(far.getMaxHealth(), far.getHealth(), "di luar jangkauan");

        PuffBullet bullet = new PuffBullet(px(1), px(6), 15);
        board.addBullet(bullet);
        seconds(board, 5);
        assertTrue(bullet.isDone());
        assertTrue(bullet.getX() < Board.WIDTH, "spora hilang sebelum keluar layar");
    }

    @Test
    void fumeShroomHitsEveryZombieInRange() {
        Board board = nightBoard();
        board.addPlant(new FumeShroom(px(1), px(2)));
        Zombie a = dummy(2, 2);
        Zombie b = dummy(4, 2);
        Zombie outside = dummy(1 + FumeShroom.RANGE + 2, 2);
        board.addZombie(a);
        board.addZombie(b);
        board.addZombie(outside);
        seconds(board, 4.2);
        assertTrue(a.getHealth() < a.getMaxHealth());
        assertTrue(b.getHealth() < b.getMaxHealth());
        assertEquals(outside.getMaxHealth(), outside.getHealth());
    }

    // ------------------------------------------------------------------ penghasil sun

    @Test
    void sunflowerDropsSunThatMustBeCollected() {
        Board board = nightBoard(); // malam: tidak ada sun dari langit
        board.addPlant(new Sunflower(px(2), px(2)));
        int before = board.getSun().get();
        seconds(board, Sunflower.FIRST_SUN + 0.1);
        assertEquals(1, board.getSuns().size());
        assertEquals(before, board.getSun().get(), "belum diambil");
        assertEquals(Sunflower.SUN_AMOUNT, board.collectAllSun());
        assertEquals(before + Sunflower.SUN_AMOUNT, board.getSun().get());
        seconds(board, Sunflower.SUN_INTERVAL);
        assertEquals(1, board.getSuns().stream().filter(SunToken::isCollectable).count());
    }

    @Test
    void sunShroomGivesSmallSunThenGrows() {
        Board board = nightBoard();
        SunShroom shroom = new SunShroom(px(2), px(2));
        board.addPlant(shroom);
        seconds(board, 6.1);
        assertEquals(SunShroom.SMALL_SUN, board.collectAllSun());
        assertFalse(shroom.isGrown());
        tick(board, SunShroom.GROW_TIME);
        assertTrue(shroom.isGrown());
        board.collectAllSun();
        seconds(board, 12.1);
        assertEquals(SunShroom.BIG_SUN, board.collectAllSun());
    }

    // ------------------------------------------------------------------ sekali pakai

    @Test
    void squashJumpsThenCrushesNearbyZombie() {
        Board board = quietBoard();
        Squash squash = new Squash(px(4), px(2));
        board.addPlant(squash);
        Zombie near = new BucketheadZombie(px(5), px(2));
        Zombie far = new BucketheadZombie(px(8), px(2));
        board.addZombie(near);
        board.addZombie(far);
        board.update();
        assertTrue(squash.isJumping());
        assertFalse(near.isDead(), "masih di udara");
        tick(board, Squash.JUMP_TICKS + 1);
        assertTrue(board.getZombies().contains(far));
        assertFalse(board.getZombies().contains(near));
        assertTrue(board.getPlants().isEmpty());
        assertEquals(DamageType.CRUSH, near.getLastDamage());
    }

    @Test
    void cherryBombExplodesIn3x3AndCannotBeEaten() {
        Board board = quietBoard();
        CherryBomb cherry = new CherryBomb(px(4), px(3));
        board.addPlant(cherry);
        Zombie eater = new NormalZombie(px(4) + 20, px(3)); // langsung memakan cherry
        Zombie diagonal = dummy(5, 4);
        Zombie outside = dummy(6, 3);
        board.addZombie(eater);
        board.addZombie(diagonal);
        board.addZombie(outside);
        seconds(board, CherryBomb.FUSE - 0.1);
        assertTrue(board.getPlants().contains(cherry), "belum meledak dan tidak bisa dimakan");
        seconds(board, 0.3);
        assertFalse(board.getPlants().contains(cherry));
        assertFalse(board.getZombies().contains(eater));
        assertFalse(board.getZombies().contains(diagonal));
        assertTrue(board.getZombies().contains(outside));
        assertEquals(DamageType.EXPLOSION, eater.getLastDamage());
    }

    @Test
    void jalapenoBurnsTheWholeRowOnly() {
        Board board = quietBoard();
        board.addPlant(new Jalapeno(px(1), px(5)));
        Zombie first = dummy(3, 5);
        Zombie last = dummy(9, 5);
        Zombie otherRow = dummy(5, 6);
        board.addZombie(first);
        board.addZombie(last);
        board.addZombie(otherRow);
        seconds(board, Jalapeno.FUSE + 0.1);
        assertFalse(board.getZombies().contains(first));
        assertFalse(board.getZombies().contains(last));
        assertTrue(board.getZombies().contains(otherRow));
        assertEquals(DamageType.FIRE, last.getLastDamage());
    }

    @Test
    void potatoMineNeedsTimeToArm() {
        Board board = quietBoard();
        PotatoMine mine = new PotatoMine(px(3), px(2));
        board.addPlant(mine);
        tick(board, PotatoMine.ARM_TIME - 1);
        assertFalse(mine.isArmed());
        assertNotEquals("potatomine", mine.getSpriteId());
        board.update();
        assertTrue(mine.isArmed());
        assertEquals("potatomine", mine.getSpriteId());
    }

    @Test
    void armedPotatoMineExplodesUnderZombie() {
        Board board = quietBoard();
        PotatoMine mine = new PotatoMine(px(3), px(2));
        board.addPlant(mine);
        tick(board, PotatoMine.ARM_TIME);
        Zombie zombie = new BucketheadZombie(px(3) + 35, px(2));
        board.addZombie(zombie);
        tick(board, 2);
        assertTrue(board.getPlants().isEmpty());
        assertTrue(board.getZombies().isEmpty());
    }

    @Test
    void unarmedPotatoMineGetsEaten() {
        Board board = quietBoard();
        PotatoMine mine = new PotatoMine(px(3), px(2));
        board.addPlant(mine);
        board.addZombie(new NormalZombie(px(3) + 30, px(2)));
        seconds(board, 1.2);
        assertTrue(board.getPlants().isEmpty(), "dimakan sebelum aktif");
        assertEquals(1, board.getZombies().size());
    }

    @Test
    void tangleKelpDrownsZombie() {
        Board board = quietBoard(game.Level.ADVENTURE.get(1));
        board.addPlant(new TangleKelp(px(4), px(3)));
        Zombie zombie = new entity.zombie.DuckyTubeZombie(px(5), px(3));
        board.addZombie(zombie);
        board.update();
        assertTrue(board.getZombies().isEmpty());
        assertTrue(board.getPlants().isEmpty());
        assertEquals(DamageType.DROWN, zombie.getLastDamage());
    }

    // ------------------------------------------------------------------ penahan

    @Test
    void wallNutCracksAsHealthDrops() {
        WallNut nut = new WallNut(px(2), px(2));
        String full = nut.getSpriteId();
        nut.takeDamage(400);
        String cracked = nut.getSpriteId();
        nut.takeDamage(400);
        String veryCracked = nut.getSpriteId();
        assertNotEquals(full, cracked);
        assertNotEquals(cracked, veryCracked);
    }

    @Test
    void hitMakesEntityFlash() {
        WallNut nut = new WallNut(px(2), px(2));
        nut.takeDamage(10);
        assertTrue(nut.isFlashing());
        for (int i = 0; i < entity.Entity.FLASH_TICKS; i++) {
            nut.tickAnimation();
        }
        assertFalse(nut.isFlashing());
    }
}
