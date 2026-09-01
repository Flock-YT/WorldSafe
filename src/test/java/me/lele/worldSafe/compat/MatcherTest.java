package me.lele.worldSafe.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatcherTest {

    @Test
    void matchesMaterialRenamesInBothDirections() {
        assertTrue(MaterialMatcher.matchesName("SOIL", "FARMLAND"));
        assertTrue(MaterialMatcher.matchesName("FARMLAND", "SOIL"));
        assertTrue(MaterialMatcher.matchesName("GRASS", "GRASS_BLOCK"));
        assertTrue(MaterialMatcher.matchesName("BED_BLOCK", "RED_BED"));
        assertTrue(MaterialMatcher.matchesName("LEGACY_BED", "BLACK_BED"));
        assertTrue(MaterialMatcher.matchesName("WEB", "COBWEB"));
        assertFalse(MaterialMatcher.matchesName("STONE", "COBWEB"));
    }

    @Test
    void matchesEntityRenamesInBothDirections() {
        assertTrue(EntityTypeMatcher.matchesName("SNOWMAN", "SNOW_GOLEM"));
        assertTrue(EntityTypeMatcher.matchesName("ENDER_CRYSTAL", "END_CRYSTAL"));
        assertTrue(EntityTypeMatcher.matchesName("PRIMED_TNT", "TNT"));
        assertTrue(EntityTypeMatcher.matchesName("MINECART_TNT", "TNT_MINECART"));
        assertTrue(EntityTypeMatcher.matchesName("SULPHUR_CUBE", "SULFUR_CUBE"));
        assertFalse(EntityTypeMatcher.matchesName("PLAYER", "WIND_CHARGE"));
    }
}
