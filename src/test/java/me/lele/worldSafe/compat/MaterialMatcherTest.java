package me.lele.worldSafe.compat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaterialMatcherTest {

    @Test
    void matchesMaterialsAddedAfterCompileBaselineByName() {
        assertTrue(MaterialMatcher.matchesName("minecraft:decorated_pot", "DECORATED_POT"));
        assertTrue(MaterialMatcher.matchesName("POINTED_DRIPSTONE", "POINTED_DRIPSTONE"));
        assertFalse(MaterialMatcher.matchesName("STONE", "DECORATED_POT", "CHORUS_FLOWER"));
    }
}
