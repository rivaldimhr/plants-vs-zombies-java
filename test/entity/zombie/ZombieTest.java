package entity.zombie;

import static game.TestUtil.px;
import static game.TestUtil.quietBoard;
import static game.TestUtil.seconds;
import static game.TestUtil.tick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import entity.plant.Plant;
import entity.plant.TallNut;
import entity.plant.WallNut;
import entity.projectile.PeaBullet;
import game.Board;
import game.Game;

class ZombieTest {

    @Test
    void normalZombieWalksOnePixelEveryElevenTicks() {
        Board board = quietBoard();
        Zombie zombie = new NormalZombie(px(9), px(2));
        board.addZombie(zombie);
        tick(board, (Zombie.WALK_DELAY + 1) * 10);
        assertEquals(px(9) - 10, zombie.getX());
    }

    @Test
    void zombieStopsAndEatsPlantEverySecond() {
        Board board = quietBoard();
        WallNut nut = new WallNut(px(5), px(2));
        board.addPlant(nut);
        Zombie zombie = new NormalZombie(px(5) + 30, px(2));
        board.addZombie(zombie);
        seconds(board, 5.1);
        assertEquals(px(5) + 30, zombie.getX());
        assertTrue(zombie.isEating());
        assertEquals(nut.getMaxHealth() - 5 * zombie.getAttackDamage(), nut.getHealth());
    }

    @Test
    void zombieEatsPlantOnTopOfLilyPadFirst() {
        Board board = quietBoard();
        entity.plant.LilyPad pad = new entity.plant.LilyPad(px(5), px(3));
        WallNut top = new WallNut(px(5), px(3));
        board.addPlant(pad);
        board.addPlant(top);
        board.addZombie(new DuckyTubeZombie(px(5) + 10, px(3)));
        seconds(board, 3.1);
        assertEquals(pad.getMaxHealth(), pad.getHealth());
        assertEquals(top.getMaxHealth() - 300, top.getHealth());
    }

    @Test
    void slowedZombieMovesAtHalfSpeedForThreeSeconds() {
        Board board = quietBoard();
        Zombie zombie = new NormalZombie(px(9), px(2));
        board.addZombie(zombie);
        zombie.applySlow();
        tick(board, Zombie.SLOW_DURATION - 1);
        assertTrue(zombie.isSlowed());
        int slowDistance = px(9) - zombie.getX();
        tick(board, 1);
        assertFalse(zombie.isSlowed());
        int normalDistance = Zombie.SLOW_DURATION / (Zombie.WALK_DELAY + 1);
        assertTrue(slowDistance <= normalDistance / 2 + 1, slowDistance + " vs " + normalDistance);
    }

    @Test
    void footballZombieIsTwiceAsFast() {
        assertEquals(Zombie.WALK_DELAY / 2, new FootballZombie(0, 0).getWalkDelay());
    }

    @Test
    void poleVaulterJumpsOverFirstPlant() {
        Board board = quietBoard();
        WallNut nut = new WallNut(px(5), px(2));
        board.addPlant(nut);
        PoleVaultingZombie zombie = new PoleVaultingZombie(px(6), px(2));
        board.addZombie(zombie);
        seconds(board, 10);
        assertFalse(zombie.hasPole());
        assertTrue(zombie.getX() < nut.getX(), "zombie harus sudah di belakang tanaman");
        assertEquals(nut.getMaxHealth(), nut.getHealth());
        assertEquals(Zombie.WALK_DELAY, zombie.getWalkDelay());
    }

    @Test
    void poleVaulterCannotJumpOverTallNut() {
        Board board = quietBoard();
        TallNut nut = new TallNut(px(5), px(2));
        board.addPlant(nut);
        PoleVaultingZombie zombie = new PoleVaultingZombie(px(6), px(2));
        board.addZombie(zombie);
        seconds(board, 10);
        assertFalse(zombie.hasPole());
        assertTrue(zombie.getX() >= nut.getX());
        assertTrue(nut.getHealth() < nut.getMaxHealth());
    }

    @Test
    void newspaperZombieEnragesWhenPaperIsDestroyed() {
        NewspaperZombie zombie = new NewspaperZombie(px(8), px(2));
        double normalAttack = zombie.getAttackSpeed();
        zombie.takeDamage(NewspaperZombie.PAPER_HEALTH - 25);
        assertFalse(zombie.isEnraged());
        zombie.takeDamage(25);
        assertTrue(zombie.isEnraged());
        assertEquals(Zombie.WALK_DELAY / 2, zombie.getWalkDelay());
        assertEquals(normalAttack / 2, zombie.getAttackSpeed());
    }

    @Test
    void snorkelCannotBeHitWhileSubmerged() {
        Board board = quietBoard();
        SnorkelZombie snorkel = new SnorkelZombie(px(6), px(3));
        board.addZombie(snorkel);
        assertFalse(snorkel.isTargetable());
        PeaBullet pea = new PeaBullet(px(5), px(3), 25);
        board.addBullet(pea);
        seconds(board, 1);
        assertEquals(snorkel.getMaxHealth(), snorkel.getHealth());
    }

    @Test
    void snorkelSurfacesWhenEating() {
        Board board = quietBoard();
        Plant pad = new entity.plant.LilyPad(px(5), px(3));
        board.addPlant(pad);
        SnorkelZombie snorkel = new SnorkelZombie(px(5) + 20, px(3));
        board.addZombie(snorkel);
        board.update();
        assertTrue(snorkel.isEating());
        assertTrue(snorkel.isTargetable());
    }

    @Test
    void dolphinRiderInstantlyKillsFirstPlantOnly() {
        Board board = quietBoard();
        Plant first = new entity.plant.LilyPad(px(6), px(4));
        WallNut second = new WallNut(px(3), px(4));
        board.addPlant(first);
        board.addPlant(second);
        DolphinRiderZombie dolphin = new DolphinRiderZombie(px(6) + 20, px(4));
        board.addZombie(dolphin);
        board.update();
        assertFalse(board.getPlants().contains(first));

        // tanaman kedua dimakan biasa (tidak langsung mati)
        for (int i = 0; i < 30 * Game.UPS && !dolphin.isEating(); i++) {
            board.update();
        }
        assertTrue(dolphin.isEating());
        assertEquals(second.getMaxHealth(), second.getHealth());
        tick(board, Game.UPS + 1);
        assertEquals(second.getMaxHealth() - dolphin.getAttackDamage(), second.getHealth());
    }

    @Test
    void attackTimingUsesAttackSpeed() {
        Board board = quietBoard();
        WallNut nut = new WallNut(px(5), px(2));
        board.addPlant(nut);
        Zombie zombie = new ConeheadZombie(px(5) + 30, px(2));
        board.addZombie(zombie);
        tick(board, (int) (zombie.getAttackSpeed() * Game.UPS));
        assertEquals(nut.getMaxHealth(), nut.getHealth());
        tick(board, 2);
        assertEquals(nut.getMaxHealth() - zombie.getAttackDamage(), nut.getHealth());
    }
}
