package me.lele.worldSafe.core;

import me.lele.worldSafe.compat.MinecraftVersion;
import java.util.Objects;

public final class FeatureSpec {
    private final String key;
    private final MinecraftVersion minimumVersion;

    public FeatureSpec(String key, MinecraftVersion minimumVersion) {
        this.key = Objects.requireNonNull(key, "key");
        this.minimumVersion = Objects.requireNonNull(minimumVersion, "minimumVersion");
    }

    public String getKey() { return key; }
    public MinecraftVersion getMinimumVersion() { return minimumVersion; }
}
