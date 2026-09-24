package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

import entity.plant.PlantType;
import entity.zombie.ZombieType;

class SpriteTest {

    private static Sprite threeFrames() {
        BufferedImage[] frames = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            frames[i] = new BufferedImage(10, 20, BufferedImage.TYPE_INT_ARGB);
            frames[i].setRGB(5, 5, 0xFF00FF00);
        }
        return new Sprite(frames, 100);
    }

    @Test
    void frameIndexLoopsOrClamps() {
        Sprite sprite = threeFrames();
        assertEquals(0, sprite.frameIndex(0, true));
        assertEquals(1, sprite.frameIndex(150, true));
        assertEquals(0, sprite.frameIndex(300, true));
        assertEquals(2, sprite.frameIndex(10_000, false));
    }

    @Test
    void frameIndexWorksWithHugeTimes() {
        // regresi: System.currentTimeMillis() dulu membuat index negatif (overflow int)
        int index = threeFrames().frameIndex(System.currentTimeMillis(), true);
        assertTrue(index >= 0 && index < 3);
    }

    @Test
    void tintKeepsShapeAndIsCached() {
        Sprite sprite = threeFrames();
        BufferedImage tinted = sprite.tinted(0, new Color(255, 255, 255, 255));
        assertEquals(0xFFFFFFFF, tinted.getRGB(5, 5));
        assertEquals(0, tinted.getRGB(0, 0) >>> 24, "piksel transparan tetap transparan");
        assertSame(tinted, sprite.tinted(0, new Color(255, 255, 255, 255)));
    }

    @Test
    void scaledSpriteIsCachedPerSize() {
        Sprite sprite = threeFrames();
        Sprite small = sprite.scaledTo(5, 10);
        assertEquals(5, small.getWidth());
        assertEquals(10, small.getHeight());
        assertEquals(3, small.getFrameCount());
        assertSame(small, sprite.scaledTo(5, 10));
        assertSame(sprite, sprite.scaledTo(10, 20));
    }

    @Test
    void everyEntityHasItsSprite() {
        for (PlantType type : PlantType.values()) {
            assertFalse(type.getPrototype().getSprite().isEmpty(), type.toString());
            assertTrue(Assets.resource(type.getCardImage()) != null, type.getCardImage());
        }
        for (ZombieType type : ZombieType.values()) {
            assertFalse(type.getPrototype().getSprite().isEmpty(), type.toString());
        }
        for (String id : new String[] { "zombie_dying", "football_dying", "pea", "snowpea_bullet", "puff_bullet",
                "sun", "lawnmower", "zombie_head", "potatomine", "wallnut_cracked2", "tallnut_cracked2" }) {
            assertFalse(Assets.sprite(id).isEmpty(), id);
        }
    }

    @Test
    void soundFilesExist() {
        for (String name : new String[] { "plant", "shoot", "hit", "sun", "explode", "mine", "fire", "mower", "chomp",
                "gulp", "groan", "siren", "finalwave", "dig", "click", "win", "lose", "music_day", "music_night" }) {
            assertTrue(Assets.resource("sound/" + name + ".wav") != null, name);
        }
    }
}
