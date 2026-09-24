package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import entity.plant.PlantType;

class DeckTest {

    private static Deck fullDeck() {
        Deck deck = new Deck();
        for (int i = 0; i < Deck.SIZE; i++) {
            deck.toggle(PlantType.values()[i]);
        }
        return deck;
    }

    @Test
    void toggleAddsAndRemoves() {
        Deck deck = new Deck();
        assertTrue(deck.toggle(PlantType.PEASHOOTER));
        assertTrue(deck.contains(PlantType.PEASHOOTER));
        assertTrue(deck.toggle(PlantType.PEASHOOTER));
        assertFalse(deck.contains(PlantType.PEASHOOTER));
    }

    @Test
    void deckHoldsAtMostSixPlants() {
        Deck deck = fullDeck();
        assertTrue(deck.isFull());
        assertFalse(deck.toggle(PlantType.values()[Deck.SIZE]));
        assertEquals(Deck.SIZE, deck.size());
        assertNull(deck.get(Deck.SIZE));
    }

    @Test
    void cooldownUsesPlantCooldownInSeconds() {
        Deck deck = fullDeck();
        assertTrue(deck.isReady(0));
        deck.startCooldown(0);
        assertFalse(deck.isReady(0));
        int seconds = deck.get(0).getCooldown();
        assertEquals(seconds, deck.getCooldownSeconds(0));
        assertEquals(1.0, deck.getCooldownFraction(0));

        for (int i = 0; i < seconds * Game.UPS - 1; i++) {
            deck.tick();
        }
        assertFalse(deck.isReady(0));
        deck.tick();
        assertTrue(deck.isReady(0));
        assertTrue(deck.isReady(1));
    }

    @Test
    void resetCooldownsMakesAllSlotsReady() {
        Deck deck = fullDeck();
        deck.startCooldown(2);
        deck.resetCooldowns();
        assertTrue(deck.isReady(2));
    }

    @Test
    void swapExchangesPositionsAndCooldowns() {
        Deck deck = fullDeck();
        PlantType first = deck.get(0);
        PlantType last = deck.get(5);
        deck.startCooldown(0);
        assertTrue(deck.swap(0, 5));
        assertEquals(last, deck.get(0));
        assertEquals(first, deck.get(5));
        assertTrue(deck.isReady(0));
        assertFalse(deck.isReady(5)); // cooldown ikut pindah bersama kartunya
    }

    @Test
    void swapRejectsInvalidSlots() {
        Deck deck = new Deck();
        deck.toggle(PlantType.PEASHOOTER);
        assertFalse(deck.swap(0, 1));
        assertFalse(deck.swap(-1, 0));
    }

    @Test
    void replacePutsNewPlantInSameSlot() {
        Deck deck = fullDeck();
        PlantType outside = PlantType.values()[Deck.SIZE];
        PlantType old = deck.get(2);
        assertTrue(deck.replace(2, outside));
        assertEquals(outside, deck.get(2));
        assertFalse(deck.contains(old));
        assertEquals(Deck.SIZE, deck.size());
        assertFalse(deck.replace(3, outside)); // tidak boleh duplikat
    }
}
