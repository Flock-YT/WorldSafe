package me.lele.worldSafe.core;

import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.config.WorldSafeConfig;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/** Immutable policy; platform hooks supply native object identities as strings. */
public final class ProtectionPolicy {
    public enum ExplosionAction { NORMAL, CANCEL, PROTECT_BLOCKS }

    private final Map<String, WorldSelector> selectors;

    public ProtectionPolicy(WorldSafeConfig config, MinecraftVersion game,
            UnaryOperator<String> normalizer) {
        Map<String, WorldSelector> active = new LinkedHashMap<String, WorldSelector>();
        if (config.isEnabled()) {
            for (FeatureSpec feature : FeatureCatalog.all().values()) {
                if (game.isAtLeast(feature.getMinimumVersion())) {
                    active.put(feature.getKey(), new WorldSelector(config.getWorlds(feature.getKey()), normalizer));
                }
            }
        }
        selectors = Collections.unmodifiableMap(active);
    }

    public boolean enabled(String key, String world) {
        WorldSelector selector = selectors.get(key);
        return selector != null && selector.matches(world);
    }

    public ExplosionAction explosion(String cancelKey, String protectKey, String world) {
        if (enabled(cancelKey, world)) { return ExplosionAction.CANCEL; }
        return enabled(protectKey, world) ? ExplosionAction.PROTECT_BLOCKS : ExplosionAction.NORMAL;
    }
}
