package game;

import static game.TestUtil.px;
import static game.TestUtil.quietBoard;
import static game.TestUtil.seconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import entity.plant.PlantType;
import entity.plant.WallNut;
import entity.zombie.NormalZombie;

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
        assertTrue(board.canPlant(PlantType.PEASHOOTER, 3, 3));
        assertFalse(board.canPlant(PlantType.LILY_PAD, 3, 3));
        assertFalse(board.canPlant(PlantType.TANGLE_KELP, 3, 3));
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
        board.plant(PlantType.WALL_NUT, 4, 3 - 1);
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
    void dayAndNightFollowTheCycle() {
        assertTrue(board.isDay());
        board.setTime(Board.DAY_LENGTH - 1);
        assertTrue(board.isDay());
        board.setTime(Board.DAY_LENGTH);
        assertFalse(board.isDay());
        board.setTime(Board.DAY_NIGHT_CYCLE);
        assertTrue(board.isDay());
    }

    @Test
    void sunOnlyFallsDuringTheDay() {
        Board b = quietBoard();
        seconds(b, Sun.MAX_DROP_DELAY);
        assertTrue(b.getSun().get() > Sun.INITIAL_SUN);

        Board night = quietBoard();
        night.setTime(Board.DAY_LENGTH);
        seconds(night, Sun.MAX_DROP_DELAY * 3);
        assertEquals(Sun.INITIAL_SUN, night.getSun().get());
    }

    @Test
    void zombieReachingHouseLosesTheGame() {
        board.addZombie(new NormalZombie(px(1), px(2)));
        board.update();
        assertEquals(Board.Result.LOST, board.getResult());
    }

    @Test
    void winAfterDurationWhenAllZombiesAreDead() {
        board.setTime(Board.GAME_DURATION);
        board.addZombie(new NormalZombie(px(8), px(2)));
        board.update();
        assertEquals(Board.Result.PLAYING, board.getResult());

        board.addPlant(new WallNut(px(2), px(2)));
        board.getZombies().get(0).setHealth(0);
        board.update();
        assertEquals(Board.Result.WON, board.getResult());
    }

    @Test
    void boardStopsUpdatingAfterGameEnds() {
        board.addZombie(new NormalZombie(px(1), px(2)));
        board.update();
        long ticks = board.getTicks();
        board.update();
        assertEquals(ticks, board.getTicks());
    }
}
