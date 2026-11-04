package me.lele.worldSafe.fabric;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class BlockAndDamagePolicyTest extends FabricTestSupport {
    @ParameterizedTest
    @CsvSource({
        "enderman,dirt,air,enderManBlockPickupProtection",
        "enderman,air,dirt,enderManBlockPickupProtection",
        "silverfish,infested_stone,air,silverfishBlockChangeProtection",
        "silverfish,stone,infested_stone,silverfishBlockChangeProtection",
        "rabbit,carrots,air,rabbitCropEatingProtection",
        "sheep,grass_block,dirt,sheepGrassEatingProtection",
        "sheep,short_grass,air,sheepGrassEatingProtection",
        "villager,wheat,air,villagerCropModificationProtection",
        "villager,air,wheat,villagerCropModificationProtection",
        "ravager,oak_leaves,air,ravagerBlockDestructionProtection",
        "fox,sweet_berry_bush,sweet_berry_bush,foxBerryHarvestProtection",
        "fox,cave_vines,cave_vines,foxBerryHarvestProtection",
        "snow_golem,air,snow,snowGolemSnowTrailPrevention",
        "wither,dirt,air,witherExplosionProtection",
        "wither_skull,dirt,air,witherExplosionProtection",
        "zombie,oak_door,air,mobDoorBreakProtection",
        "pig,farmland,dirt,cropTrampleProtection"
    })
    void nativeBlockChangesUseTheirActorAndDimension(String type, String before, String after, String key) throws Exception {
        configure(key);
        overworld.blocks.put(position, "minecraft:" + before);
        nether.blocks.put(position, "minecraft:" + before);
        Entity actor = entity(type);
        assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:" + after, actor));
        assertFalse(BlockPolicy.blockChange(nether, position, "minecraft:" + after, actor));
        configure();
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:" + after, actor));
    }

    @Test void actorAndReasonContextsRestoreTheirPreviousValues() throws Exception {
        configure("sheepGrassEatingProtection", "enderDragonBlockDestructionProtection");
        overworld.blocks.put(position, "minecraft:grass_block");
        Object original = BlockPolicy.pushActor(entity("sheep"));
        assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:dirt", null));
        Object previous = BlockPolicy.pushActor(entity("pig"));
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:dirt", null));
        BlockPolicy.restoreActor(previous);
        assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:dirt", null));
        BlockPolicy.restoreActor(original);
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:dirt", null));
        Entity dragon = entity("ender_dragon");
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:air", dragon));
        String old = BlockPolicy.pushReason("dragon");
        try { assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:air", dragon)); }
        finally { BlockPolicy.restoreReason(old); }
        // Portal generation and other work during dragon death are outside the destruction helper.
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:bedrock", dragon));
    }

    @Test void protectionsDoNotBroadenToUnrelatedEntityChanges() throws Exception {
        configure("rabbitCropEatingProtection", "silverfishBlockChangeProtection", "sheepGrassEatingProtection", "mobDoorBreakProtection");
        overworld.blocks.put(position, "minecraft:stone");
        for (String actor : new String[] {"rabbit", "silverfish", "sheep", "player"}) {
            assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:air", entity(actor)));
        }
        overworld.blocks.put(position, "minecraft:oak_door");
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:air", entity("player")));
    }

    @Test void spreadAndIgnitionRemainSeparateAndNaturalFireExtinguishingWorks() throws Exception {
        configure("fireSpreadPrevention");
        overworld.blocks.put(position, "minecraft:oak_planks");
        String previous = BlockPolicy.pushReason("fire");
        assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:air", null));
        overworld.blocks.put(position, "minecraft:fire");
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:air", null));
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:fire", null));
        BlockPolicy.restoreReason(previous);
        overworld.blocks.put(position, "minecraft:air");
        previous = BlockPolicy.pushReason("lava");
        assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:fire", null));
        BlockPolicy.restoreReason(previous);
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:fire", entity("player")));
        configure("fireIgnitionPrevention");
        for (String type : new String[] {"lightning_bolt", "fireball", "small_fireball", "end_crystal", "arrow", "spectral_arrow"}) {
            assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:fire", entity(type)));
        }
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:fire", entity("player")));
        previous = BlockPolicy.pushReason("lava");
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:fire", null));
        BlockPolicy.restoreReason(previous);
    }

    @Test void windProtectionDoesNotSuppressOtherProjectilesOrCampfireInteractions() throws Exception {
        configure("windChargeBlockDestructionProtection");
        Entity arrow = entity("arrow").projectile(null);
        assertFalse(BlockPolicy.cancelProjectileBlock(overworld, arrow, true, false));
        assertTrue(BlockPolicy.cancelProjectileBlock(overworld, entity("wind_charge"), true, false));
        assertFalse(BlockPolicy.cancelProjectileBlock(overworld, entity("wind_charge"), false, true));
        configure("decoratedPotProjectileProtection");
        assertTrue(BlockPolicy.cancelProjectileBlock(overworld, arrow, true, false));
        overworld.blocks.put(position, "minecraft:decorated_pot");
        assertTrue(BlockPolicy.blockChange(overworld, position, "minecraft:air", arrow));
        assertFalse(BlockPolicy.blockChange(overworld, position, "minecraft:air", entity("player")));
    }

    @Test void directDamageDistinguishesPhantomsGhastFireballsAndBreezeProjectiles() throws Exception {
        configure("phantomDamagePrevention", "ghastExplosionCancel", "breezeWindChargeImpactCancel");
        Entity victim = entity("pig");
        Entity ghast = entity("ghast");
        assertTrue(DamagePolicy.cancel(victim, entity("phantom"), entity("phantom")));
        assertTrue(DamagePolicy.cancel(victim, ghast, entity("fireball").projectile(ghast)));
        assertTrue(DamagePolicy.cancel(victim, null, entity("small_fireball").projectile(ghast)));
        assertFalse(DamagePolicy.cancel(victim, entity("blaze"), entity("small_fireball").projectile(entity("blaze"))));
        assertTrue(DamagePolicy.cancel(victim, entity("breeze"), entity("breeze_wind_charge")));
        assertFalse(DamagePolicy.cancel(victim, entity("player"), entity("wind_charge")));
        assertFalse(DamagePolicy.cancel(new Entity(nether, "minecraft:pig"), ghast, entity("fireball").projectile(ghast)));
        assertFalse(DamagePolicy.cancel(new Object(), ghast, entity("fireball")));
        configure("ghastExplosionProtection");
        assertFalse(DamagePolicy.cancel(victim, ghast, entity("fireball").projectile(ghast)));
    }
}
