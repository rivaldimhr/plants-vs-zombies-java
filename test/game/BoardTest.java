package game;

import static game.TestUtil.landSuns;
import static game.TestUtil.nightBoard;
import static game.TestUtil.px;
import static game.TestUtil.quietBoard;
import static game.TestUtil.seconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entity.LawnMower;
import entity.SunToken;
import entity.effect.DeathEffect;
import entity.plant.PlantType;
import entity.zombie.NormalZombie;
import entity.zombie.Zombie;

class BoardTest {
    private Board board;

    @BeforeEach
    void setUp() {
        board = quietBoard();
        board.getSun().add(10_000);
    }

    @Test
    void cannotPlantInHouseOrSpawnColumnOrHudRow() {
        assertFalse(board.canPlant(PlantType.PEASHOOTER, 0, 1));
        assertFalse(board.canPlant(PlantType.PEASHOOTER, Board.COLS - 1, 1));
        assertFalse(board.canPlant(PlantType.PEASHOOTER, 3, 0));
        assertTrue(board.canPlant(PlantType.PEASHOOTER, 1, 1));
        assertTrue(board.canPlant(PlantType.PEASHOOTER, Board.COLS - 2, 6));
    }

    @Test
    void landPlantsOnlyOnLandAndAquaticPlantsOnlyInPool() {
        assertFalse(board.canPlant(PlantType.PEASHOOTER, 3, 3));
        assertFalse(board.canPlant(PlantType.LILY_PAD, 3, 2));
        assertFalse(board.canPlant(PlantType.TANGLE_KELP, 3, 5));
        assertTrue(board.canPlant(PlantType.LILY_PAD, 3, 3));
        assertTrue(board.canPlant(PlantType.TANGLE_KELP, 3, 4));
    }

    @Test
    void landPlantCanBePlacedOnLilyPadButNothingElse() {
        assertTrue(board.plant(PlantType.LILY_PAD, 3, 3));
        assertTrue(board.canPlant(PlantType.CHERRY_BOMB, 3, 3));
        assertFalse(board.canPlant(PlantType.LILY_PAD, 3, 3));
        assertTrue(board.plant(PlantType.PEASHOOTER, 3, 3));
        assertFalse(board.canPlant(PlantType.WALL_NUT, 3, 3));
        assertEquals(2, board.getPlantsAt(3, 3).size());
    }

    @Test
    void occupiedTileCannotBePlanted() {
        assertTrue(board.plant(PlantType.SUNFLOWER, 2, 2));
        assertFalse(board.plant(PlantType.PEASHOOTER, 2, 2));
    }

    @Test
    void plantingSpendsSunAndFailsWhenNotEnough() {
        Board fresh = quietBoard();
        assertEquals(Sun.INITIAL_SUN, fresh.getSun().get());
        assertFalse(fresh.plant(PlantType.PEASHOOTER, 2, 2)); // butuh 100
        assertEquals(50, fresh.getSun().get());
        assertTrue(fresh.plant(PlantType.SUNFLOWER, 2, 2));
        assertEquals(0, fresh.getSun().get());
        assertTrue(fresh.plant(PlantType.PUFF_SHROOM, 3, 2)); // gratis
    }

    @Test
    void digRemovesEveryPlantOnTileOnly() {
        board.plant(PlantType.LILY_PAD, 3, 3);
        board.plant(PlantType.PEASHOOTER, 3, 3);
        board.plant(PlantType.WALL_NUT, 4, 2);
        assertTrue(board.dig(3, 3));
        assertTrue(board.getPlantsAt(3, 3).isEmpty());
        assertEquals(1, board.getPlants().size());
        assertFalse(board.dig(5, 5));
    }

    @Test
    void timeAdvancesOneSecondPerSixtyTicks() {
        TestUtil.tick(board, Game.UPS - 1);
        assertEquals(0, board.getTime());
        TestUtil.tick(board, 1);
        assertEquals(1, board.getTime());
    }

    @Test
    void nightLevelsAreNotDay() {
        assertTrue(board.isDay());
        assertFalse(nightBoard().isDay());
    }

    // ------------------------------------------------------------------ sun

    @Test
    void skySunFallsOnlyOnDayLevels() {
        Board day = new Board(Level.ADVENTURE.get(0), new Random(1));
        seconds(day, Board.SKY_SUN_MAX / (double) Game.UPS + 5);
        assertFalse(day.getSuns().isEmpty());

        Board night = new Board(Level.ADVENTURE.get(3), new Random(1));
        seconds(night, 18);
        assertTrue(night.getSuns().isEmpty());
    }

    @Test
    void sunMustBeClickedToCollect() {
        SunToken token = SunToken.fromSky(200, 150, 25);
        board.addSun(token);
        landSuns(board);
        int before = board.getSun().get();
        assertFalse(board.collectSunAt(50, 50));
        assertTrue(board.collectSunAt(token.getX(), token.getY()));
        assertEquals(before + 25, board.getSun().get());
        assertFalse(board.collectSunAt(token.getX(), token.getY()), "tidak bisa diambil dua kali");
        seconds(board, 1);
        assertTrue(board.getSuns().isEmpty(), "sun terbang ke bank lalu hilang");
    }

    @Test
    void uncollectedSunDisappears() {
        board.addSun(SunToken.fromSky(200, 150, 25));
        landSuns(board);
        TestUtil.tick(board, SunToken.LIFETIME + 1);
        assertTrue(board.getSuns().isEmpty());
    }

    @Test
    void collectAllSunReturnsTotal() {
        board.addSun(SunToken.fromSky(200, 150, 25));
        board.addSun(SunToken.fromPlant(300, 200, 15, 0.5));
        assertEquals(40, board.collectAllSun());
    }

    // ------------------------------------------------------------------ lawn mower & hasil

    @Test
    void lawnMowerSavesTheRowOnce() {
        board.addZombie(new NormalZombie(LawnMower.TRIGGER_X, px(2)));
        board.addZombie(new NormalZombie(px(7), px(2)));
        seconds(board, 3);
        assertEquals(Board.Result.PLAYING, board.getResult());
        assertTrue(board.getZombies().isEmpty(), "mesin membersihkan seluruh baris");
        assertEquals(LawnMower.State.GONE, board.getMower(2).getState());
        assertEquals(LawnMower.State.IDLE, board.getMower(3).getState());
    }

    @Test
    void zombieReachingHouseWithoutMowerLosesTheGame() {
        board.addZombie(new NormalZombie(LawnMower.TRIGGER_X, px(2)));
        seconds(board, 3);
        board.addZombie(new NormalZombie(Board.LOSE_X, px(2)));
        board.update();
        assertEquals(Board.Result.LOST, board.getResult());
    }

    @Test
    void winAfterAllWavesWhenAllZombiesAreDead() {
        Zombie zombie = new NormalZombie(px(8), px(2));
        board.addZombie(zombie);
        TestUtil.finishWaves(board);
        board.update();
        assertEquals(Board.Result.PLAYING, board.getResult());
        zombie.setHealth(0);
        board.update();
        assertEquals(Board.Result.WON, board.getResult());
    }

    @Test
    void endlessCanNeverBeWon() {
        Board endless = quietBoard(Level.ENDLESS);
        TestUtil.finishWaves(endless);
        TestUtil.tick(endless, 10);
        assertEquals(Board.Result.PLAYING, endless.getResult());
    }

    @Test
    void boardStopsUpdatingAfterGameEnds() {
        board.addZombie(new NormalZombie(LawnMower.TRIGGER_X, px(2)));
        seconds(board, 3);
        board.addZombie(new NormalZombie(Board.LOSE_X, px(2)));
        board.update();
        long ticks = board.getTicks();
        board.update();
        assertEquals(ticks, board.getTicks());
    }

    // ------------------------------------------------------------------ event & efek

    @Test
    void eventsAreSentToListeners() {
        List<GameEvent> events = new ArrayList<>();
        board.addListener(events::add);
        board.plant(PlantType.WALL_NUT, 2, 2);
        board.dig(2, 2);
        board.addSun(SunToken.fromSky(100, 100, 25));
        board.collectAllSun();
        assertEquals(List.of(GameEvent.PLANT, GameEvent.DIG, GameEvent.SUN_COLLECTED), events);
    }

    @Test
    void deadZombieIsCountedAndLeavesDeathEffect() {
        Zombie zombie = new NormalZombie(px(8), px(2));
        board.addZombie(zombie);
        zombie.takeDamage(1000);
        board.update();
        assertEquals(1, board.getZombiesKilled());
        assertTrue(board.getZombies().isEmpty());
        assertFalse(board.getEffects().isEmpty());
        seconds(board, 5);
        assertTrue(board.getEffects().isEmpty(), "efek hilang setelah selesai");
    }

    @Test
    void explosionKillLeavesBurntEffect() {
        Zombie zombie = new NormalZombie(px(8), px(2));
        board.addZombie(zombie);
        zombie.takeDamage(5000, entity.DamageType.EXPLOSION);
        board.update();
        assertInstanceOf(DeathEffect.class, board.getEffects().get(0));
    }
}
