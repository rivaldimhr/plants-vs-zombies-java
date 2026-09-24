package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import entity.plant.Plant;
import entity.plant.PlantType;
import entity.zombie.Zombie;
import entity.zombie.ZombieType;

/**
 * Stat harus sama dengan dokumentasi in-game (gambar di image/IMAGE/*DECK.png dan
 * gambar Zombies List). Kalau stat diubah, ubah juga gambarnya (atau test ini).
 */
class StatsTest {

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            // type, health, damage, attackSpeed, cost, cooldown, aquatic
            "PEASHOOTER,  100,   25, 4, 100, 10, false",
            "SUNFLOWER,   100,    0, 0,  50, 10, false",
            "WALL_NUT,   1000,    0, 0,  50, 20, false",
            "SNOW_PEA,    100,   25, 4, 175, 10, false",
            "SQUASH,      100, 5000, 0,  50, 20, false",
            "LILY_PAD,    100,    0, 0,  25, 10, true",
            "TALL_NUT,   2000,    0, 0, 125, 30, false",
            "PUFF_SHROOM, 100,   15, 4,   0,  7, false",
            "TANGLE_KELP, 100, 2000, 0,  25, 15, true",
            "REPEATER,    100,   25, 2, 200, 10, false",
    })
    void plantStatsMatchDocumentation(PlantType type, int health, int damage, double speed, int cost, int cooldown,
            boolean aquatic) {
        Plant plant = type.create(0, 0);
        assertEquals(health, plant.getHealth());
        assertEquals(damage, plant.getAttackDamage());
        assertEquals(speed, plant.getAttackSpeed());
        assertEquals(cost, plant.getCost());
        assertEquals(cooldown, plant.getCooldown());
        assertEquals(aquatic, plant.isAquatic());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource({
            // type, health, damage, attackSpeed, aquatic
            "NORMAL,              125, 100, 1, false",
            "CONEHEAD,            250, 100, 1, false",
            "BUCKETHEAD,          300, 100, 1, false",
            "FOOTBALL,            300, 100, 1, false",
            "NEWSPAPER,           200, 100, 1, false",
            "POLE_VAULTING,       175, 100, 1, false",
            "DUCKY_TUBE,          100, 100, 1, true",
            "DUCKY_TUBE_CONEHEAD, 250, 100, 1, true",
            "SNORKEL,             100, 100, 1, true",
            "DOLPHIN_RIDER,       175, 100, 1, true",
    })
    void zombieStatsMatchDocumentation(ZombieType type, int health, int damage, double speed, boolean aquatic) {
        Zombie zombie = type.create(0, 0);
        assertEquals(health, zombie.getHealth());
        assertEquals(damage, zombie.getAttackDamage());
        assertEquals(speed, zombie.getAttackSpeed());
        assertEquals(aquatic, zombie.isAquatic());
    }

    @ParameterizedTest
    @CsvSource({ "PEASHOOTER", "SUNFLOWER", "LILY_PAD" })
    void factoryPlacesPlantAtPosition(PlantType type) {
        Plant plant = type.create(120, 180);
        assertEquals(120, plant.getX());
        assertEquals(180, plant.getY());
        assertEquals(2, plant.getCol());
        assertEquals(3, plant.getRow());
    }
}
