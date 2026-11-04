package me.lele.worldSafe.fabric;

public final class DamagePolicy {
    private DamagePolicy() { }

    public static boolean cancel(Object victim, Object attacker, Object direct) {
        Object world = BlockPolicy.entityWorld(victim);
        if (world == null || FabricServices.policy(world) == null) { return false; }
        NativeAccess access = FabricServices.nativeAccess();
        String attacking = access.entityType(attacker);
        String projectile = access.entityType(direct);
        if (("minecraft:phantom".equals(attacking) || "minecraft:phantom".equals(projectile))
                && FabricServices.enabled("phantomDamagePrevention", world)) { return true; }
        if (("minecraft:fireball".equals(projectile) || "minecraft:small_fireball".equals(projectile))
                && ("minecraft:ghast".equals(attacking)
                    || "minecraft:ghast".equals(access.entityType(access.projectileOwner(direct))))
                && FabricServices.enabled("ghastExplosionCancel", world)) { return true; }
        return "minecraft:breeze_wind_charge".equals(projectile)
                && FabricServices.enabled("breezeWindChargeImpactCancel", world);
    }
}
