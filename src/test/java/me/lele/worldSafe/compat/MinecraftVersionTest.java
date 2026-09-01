package me.lele.worldSafe.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftVersionTest {

    @Test
    void parsesLegacyModernAndFutureVersionStrings() {
        assertEquals(new MinecraftVersion(1, 8, 8), MinecraftVersion.parse("1.8.8-R0.1-SNAPSHOT"));
        assertEquals(new MinecraftVersion(1, 21, 11),
                MinecraftVersion.parse("git-Paper-123 (MC: 1.21.11)"));
        assertEquals(new MinecraftVersion(26, 2, 0), MinecraftVersion.parse("26.2-R0.1-SNAPSHOT"));
    }

    @Test
    void comparesVersionComponentsNumerically() {
        assertTrue(MinecraftVersion.V1_20_3.isAtLeast(MinecraftVersion.V1_16));
        assertTrue(MinecraftVersion.V26_2.isAtLeast(MinecraftVersion.V1_21));
    }
}
