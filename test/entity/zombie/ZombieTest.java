package entity.zombie;

import static game.TestUtil.px;
import static game.TestUtil.quietBoard;
import static game.TestUtil.seconds;
import static game.TestUtil.tick;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import entity.DamageType;
import entity.effect.ArmorDropEffect;
import entity.effect.DeathEffect;
import entity.effect.SpriteEffect;
import entity.plant.LilyPad;
import entity.plant.Plant;
import entity.plant.TallNut;
import entity.plant.WallNut;
import entity.projectile.PeaBullet;
import game.Board;
import game.Game;
import game.GameEvent;

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
        List<GameEvent> events = new ArrayList<>();
        board.addListener(events::add);
        WallNut nut = new WallNut(px(5), px(2));
        board.addPlant(nut);
        Zombie zombie = new NormalZombie(px(5) + 30, px(2));
        board.addZombie(zombie);
        seconds(board, 5.1);
        assertEquals(px(5) + 30, zombie.getX());
        assertTrue(zombie.isEating());
        assertEquals(nut.getMaxHealth() - 5 * zombie.getAttackDamage(), nut.getHealth());
        assertTrue(events.contains(GameEvent.CHOMP));
    }

    @Test
    void zombieEatsPlantOnTopOfLilyPadFirst() {
        Board board = quietBoard();
        LilyPad pad = new LilyPad(px(5), px(3));
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

    // ------------------------------------------------------------------ armor

    @Test
    void coneAbsorbsDamageFirstThenFalls() {
        Board board = quietBoard();
        ConeheadZombie zombie = new ConeheadZombie(px(8), px(2));
        board.addZombie(zombie);
        String armored = zombie.getSpriteId();
        int body = zombie.getHealth();
        zombie.takeDamage(100);
        assertEquals(body, zombie.getHealth(), "badan belum kena");
        zombie.takeDamage(50); // cone 125: sisa 25 tembus ke badan
        assertNull(zombie.getArmor());
        assertEquals(body - 25, zombie.getHealth());
        assertNotEquals(armored, zombie.getSpriteId(), "berubah jadi zombie biasa");
        board.update();
        assertTrue(board.getEffects().stream().anyMatch(e -> e instanceof ArmorDropEffect), "cone jatuh");
    }

    @Test
    void totalHealthIncludesArmor() {
        BucketheadZombie zombie = new BucketheadZombie(0, 0);
        assertEquals(zombie.getHealth() + zombie.getArmor().getHealth(), zombie.getTotalHealth());
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

    // ------------------------------------------------------------------ kemampuan khusus

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
    void snorkelCannotBeHitWhileSubmerged() {
        Board board = quietBoard();
        SnorkelZombie snorkel = new SnorkelZombie(px(6), px(3));
        board.addZombie(snorkel);
        assertFalse(snorkel.isTargetable());
        board.addBullet(new PeaBullet(px(5), px(3), 25));
        seconds(board, 1);
        assertEquals(snorkel.getMaxHealth(), snorkel.getHealth());
    }

    @Test
    void snorkelSurfacesWhenEating() {
        Board board = quietBoard();
        Plant pad = new LilyPad(px(5), px(3));
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
        Plant first = new LilyPad(px(6), px(4));
        WallNut second = new WallNut(px(3), px(4));
        board.addPlant(first);
        board.addPlant(second);
        DolphinRiderZombie dolphin = new DolphinRiderZombie(px(6) + 20, px(4));
        board.addZombie(dolphin);
        board.update();
        assertFalse(board.getPlants().contains(first));

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

    // ------------------------------------------------------------------ animasi mati

    @Test
    void deathEffectDependsOnHowZombieDied() {
        NormalZombie shot = new NormalZombie(px(5), px(2));
        shot.takeDamage(1000);
        assertInstanceOf(SpriteEffect.class, shot.createDeathEffect(), "animasi mati normal");

        NormalZombie burnt = new NormalZombie(px(5), px(2));
        burnt.takeDamage(1800, DamageType.EXPLOSION);
        assertInstanceOf(DeathEffect.class, burnt.createDeathEffect());

        PoleVaultingZombie pole = new PoleVaultingZombie(px(5), px(2));
        pole.kill(DamageType.MOWER);
        assertTrue(pole.isDead());
        assertInstanceOf(DeathEffect.class, pole.createDeathEffect());
    }
}
