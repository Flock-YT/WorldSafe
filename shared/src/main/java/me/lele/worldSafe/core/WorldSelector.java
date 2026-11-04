package me.lele.worldSafe.core;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class WorldSelector {
    private final Set<String> worlds;
    private final UnaryOperator<String> normalizer;

    public WorldSelector(Collection<String> names, UnaryOperator<String> normalizer) {
        this.normalizer = normalizer;
        Set<String> normalized = new LinkedHashSet<String>();
        if (names != null) {
            for (String name : names) {
                if (name != null && !name.trim().isEmpty()) {
                    normalized.add(normalizer.apply(name));
                }
            }
        }
        worlds = Collections.unmodifiableSet(normalized);
    }

    public boolean matches(String world) {
        return world != null && worlds.contains(normalizer.apply(world));
    }

    public Set<String> names() { return worlds; }

    public static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    public static String fabricDimension(String name) {
        String normalized = normalize(name);
        if ("world".equals(normalized)) { return "minecraft:overworld"; }
        if ("world_nether".equals(normalized)) { return "minecraft:the_nether"; }
        if ("world_the_end".equals(normalized)) { return "minecraft:the_end"; }
        return normalized;
    }

    public static boolean isFabricDimension(String name) {
        return name != null && fabricDimension(name).matches("[a-z0-9_.-]+:[a-z0-9/._-]+");
    }
}
