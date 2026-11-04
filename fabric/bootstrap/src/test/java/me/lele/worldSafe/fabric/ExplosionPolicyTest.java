package me.lele.worldSafe.fabric;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static me.lele.worldSafe.core.ProtectionPolicy.ExplosionAction.*;
import static org.junit.jupiter.api.Assertions.*;

class ExplosionPolicyTest extends FabricTestSupport {
    @ParameterizedTest
    @CsvSource({
        "tnt,tntExplosionCancel,tntExplosionProtection",
        "tnt_minecart,tntExplosionCancel,tntExplosionProtection",
        "creeper,creeperExplosionCancel,creeperExplosionProtection",
        "end_crystal,endCrystalExplosionCancel,endCrystalExplosionPrevention",
        "wither,witherExplosionCancel,witherExplosionProtection",
        "wither_skull,witherExplosionCancel,witherExplosionProtection",
        "fireball,ghastExplosionCancel,ghastExplosionProtection",
        "small_fireball,ghastExplosionCancel,ghastExplosionProtection",
        "sulfur_cube,sulfurCubeExplosionCancel,sulfurCubeExplosionProtection",
        "sulphur_cube,sulfurCubeExplosionCancel,sulfurCubeExplosionProtection"
    })
    void explosionSourcesRespectCancellationPrecedenceWorldScopeAndDisabledDefaults(String type, String cancel, String protect) throws Exception {
        Entity source = entity(type).projectile(entity("ghast"));
        configure(cancel, protect);
        assertEquals(CANCEL, ExplosionPolicy.action(overworld, source));
        assertEquals(NORMAL, ExplosionPolicy.action(nether, source));
        configure(protect);
        assertEquals(PROTECT_BLOCKS, ExplosionPolicy.action(overworld, source));
        configure();
        assertEquals(NORMAL, ExplosionPolicy.action(overworld, source));
    }

    @Test void fireballsFromOtherOwnersAreUnaffected() throws Exception {
        configure("ghastExplosionCancel");
        assertEquals(NORMAL, ExplosionPolicy.action(overworld, entity("fireball").projectile(entity("blaze"))));
        assertEquals(NORMAL, ExplosionPolicy.action(overworld, entity("fireball")));
    }

    @Test void legacyNullExplosionSourcesRetainTheirNativeIdentityAndRestoreNestedContexts() throws Exception {
        configure("ghastExplosionCancel", "endCrystalExplosionPrevention");
        Entity fireball = entity("fireball").projectile(entity("ghast"));
        Object original = ExplosionPolicy.pushEntitySource(fireball);
        try {
            assertEquals(CANCEL, ExplosionPolicy.action(overworld, null));
            Object previous = ExplosionPolicy.pushEntitySource(entity("end_crystal"));
            try { assertEquals(PROTECT_BLOCKS, ExplosionPolicy.action(overworld, null)); }
            finally { ExplosionPolicy.restoreEntitySource(previous); }
            assertEquals(CANCEL, ExplosionPolicy.action(overworld, null));
        } finally { ExplosionPolicy.restoreEntitySource(original); }
        assertEquals(NORMAL, ExplosionPolicy.action(overworld, null));
    }

    @Test void bedsAndAnchorsAreAttributedOnlyInsideTheirSynchronousUse() throws Exception {
        configure("bedExplosionCancel", "respawnAnchorExplosionPrevention");
        String previous = ExplosionPolicy.pushBlockSource("bed");
        try {
            assertEquals(CANCEL, ExplosionPolicy.action(overworld, null));
            String nested = ExplosionPolicy.pushBlockSource("anchor");
            try { assertEquals(PROTECT_BLOCKS, ExplosionPolicy.action(overworld, null)); }
            finally { ExplosionPolicy.restoreBlockSource(nested); }
            assertEquals(CANCEL, ExplosionPolicy.action(overworld, null));
            assertEquals(NORMAL, ExplosionPolicy.action(overworld, entity("tnt")));
        } finally { ExplosionPolicy.restoreBlockSource(previous); }
        assertEquals(NORMAL, ExplosionPolicy.action(overworld, null));
    }

    @Test void breezeCancellationDoesNotCancelPlayerWindCharges() throws Exception {
        configure("breezeWindChargeImpactCancel");
        assertEquals(CANCEL, ExplosionPolicy.action(overworld, entity("breeze_wind_charge")));
        assertEquals(NORMAL, ExplosionPolicy.action(overworld, entity("wind_charge").projectile(entity("player"))));
    }

    @ParameterizedTest
    @CsvSource({"decorated_pot,true", "chorus_flower,true", "pointed_dripstone,true", "stone,false", "lever,false", "campfire,false"})
    void windBlockFilteringPreservesUnrelatedInteractions(String block, boolean protectedBlock) throws Exception {
        configure("windChargeBlockDestructionProtection");
        overworld.blocks.put(position, "minecraft:" + block);
        for (String type : new String[] {"wind_charge", "breeze_wind_charge"}) {
            assertEquals(protectedBlock, ExplosionPolicy.protectWindChargeBlock(overworld, entity(type), position));
        }
        assertFalse(ExplosionPolicy.protectWindChargeBlock(overworld, entity("arrow"), position));
        assertFalse(ExplosionPolicy.protectWindChargeBlock(nether, entity("wind_charge"), position));
    }
}
