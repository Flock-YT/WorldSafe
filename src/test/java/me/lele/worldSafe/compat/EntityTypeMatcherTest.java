package me.lele.worldSafe.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTypeMatcherTest {

    @Test
    void matchesRenamedEntityTypeAliases() {
        assertTrue(EntityTypeMatcher.matchesName("PRIMED_TNT", "PRIMED_TNT", "TNT"));
        assertTrue(EntityTypeMatcher.matchesName("minecraft:tnt", "PRIMED_TNT", "TNT"));
        assertTrue(EntityTypeMatcher.matchesName("MINECART_TNT", "MINECART_TNT", "TNT_MINECART"));
        assertTrue(EntityTypeMatcher.matchesName("TNT_MINECART", "MINECART_TNT", "TNT_MINECART"));
        assertTrue(EntityTypeMatcher.matchesName("ENDER_CRYSTAL", "ENDER_CRYSTAL", "END_CRYSTAL"));
        assertTrue(EntityTypeMatcher.matchesName("END_CRYSTAL", "ENDER_CRYSTAL", "END_CRYSTAL"));
        assertTrue(EntityTypeMatcher.matchesName("SNOW_GOLEM", "SNOWMAN", "SNOW_GOLEM"));
        assertFalse(EntityTypeMatcher.matchesName("PLAYER", "WIND_CHARGE", "BREEZE_WIND_CHARGE"));
    }

    @Test
    void normalizesFutureNamesWithoutReferencingTheirEnums() {
        assertTrue(EntityTypeMatcher.matchesName("breeze-wind-charge", "BREEZE_WIND_CHARGE"));
        assertTrue(EntityTypeMatcher.matchesName("minecraft:sulphur_cube", "SULFUR_CUBE", "SULPHUR_CUBE"));
    }
}
