package me.lele.worldSafe.fabric;

import me.lele.worldSafe.core.ProtectionPolicy;
import me.lele.worldSafe.core.ProtectionPolicy.ExplosionAction;

public final class ExplosionPolicy {
    private static final ThreadLocal<String> BLOCK_SOURCE = new ThreadLocal<String>();
    private static final ThreadLocal<Object> ENTITY_SOURCE = new ThreadLocal<Object>();

    private ExplosionPolicy() { }

    public static void blockSource(String source) { BLOCK_SOURCE.set(source); }
    public static void clearBlockSource() { BLOCK_SOURCE.remove(); }
    public static String pushBlockSource(String source) {
        String previous = BLOCK_SOURCE.get();
        BLOCK_SOURCE.set(source);
        return previous;
    }
    public static void restoreBlockSource(String previous) {
        if (previous == null) { BLOCK_SOURCE.remove(); } else { BLOCK_SOURCE.set(previous); }
    }
    public static Object pushEntitySource(Object source) {
        Object previous = ENTITY_SOURCE.get();
        ENTITY_SOURCE.set(source);
        return previous;
    }
    public static void restoreEntitySource(Object previous) {
        if (previous == null) { ENTITY_SOURCE.remove(); } else { ENTITY_SOURCE.set(previous); }
    }

    public static ExplosionAction action(Object world, Object entity) {
        if (entity == null) { entity = ENTITY_SOURCE.get(); }
        ProtectionPolicy policy = FabricServices.policy(world);
        if (policy == null) { return ExplosionAction.NORMAL; }
        NativeAccess access = FabricServices.nativeAccess();
        String type = access.entityType(entity);
        String cancel = null;
        String protect = null;
        if ("minecraft:tnt".equals(type) || "minecraft:tnt_minecart".equals(type)) {
            cancel = "tntExplosionCancel"; protect = "tntExplosionProtection";
        } else if ("minecraft:creeper".equals(type)) {
            cancel = "creeperExplosionCancel"; protect = "creeperExplosionProtection";
        } else if ("minecraft:end_crystal".equals(type) || (entity == null && "crystal".equals(BLOCK_SOURCE.get()))) {
            cancel = "endCrystalExplosionCancel"; protect = "endCrystalExplosionPrevention";
        } else if ("minecraft:wither".equals(type) || "minecraft:wither_skull".equals(type)) {
            cancel = "witherExplosionCancel"; protect = "witherExplosionProtection";
        } else if (("minecraft:fireball".equals(type) || "minecraft:small_fireball".equals(type))
                && "minecraft:ghast".equals(access.entityType(access.projectileOwner(entity)))) {
            cancel = "ghastExplosionCancel"; protect = "ghastExplosionProtection";
        } else if ("minecraft:breeze_wind_charge".equals(type)) {
            cancel = "breezeWindChargeImpactCancel";
        } else if ("minecraft:sulfur_cube".equals(type) || "minecraft:sulphur_cube".equals(type)) {
            cancel = "sulfurCubeExplosionCancel"; protect = "sulfurCubeExplosionProtection";
        } else if (entity == null && "bed".equals(BLOCK_SOURCE.get())) {
            cancel = "bedExplosionCancel"; protect = "bedExplosionProtection";
        } else if (entity == null && "anchor".equals(BLOCK_SOURCE.get())) {
            cancel = "respawnAnchorExplosionCancel"; protect = "respawnAnchorExplosionPrevention";
        }
        return policy.explosion(cancel, protect, access.dimension(world));
    }

    public static boolean protectWindChargeBlock(Object world, Object entity, Object position) {
        NativeAccess access = FabricServices.nativeAccess();
        String type = access.entityType(entity);
        if (!("minecraft:wind_charge".equals(type) || "minecraft:breeze_wind_charge".equals(type))
                || !FabricServices.enabled("windChargeBlockDestructionProtection", world)) { return false; }
        String block = access.blockType(world, position);
        return "minecraft:decorated_pot".equals(block) || "minecraft:chorus_flower".equals(block)
                || "minecraft:pointed_dripstone".equals(block);
    }
}
