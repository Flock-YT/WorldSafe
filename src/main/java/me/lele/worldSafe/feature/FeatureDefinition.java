package me.lele.worldSafe.feature;

import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.compat.ServerCapabilities;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public final class FeatureDefinition {

    public interface ListenerFactory {
        Listener create(List<String> worlds, ServerCapabilities capabilities);
    }

    private final String configKey;
    private final MinecraftVersion minimumVersion;
    private final EnumSet<ServerCapabilities.Capability> requiredCapabilities;
    private final String[] requiredMaterials;
    private final String[] requiredEntityTypes;
    private final ListenerFactory factory;

    public FeatureDefinition(String configKey, MinecraftVersion minimumVersion,
            ServerCapabilities.Capability[] requiredCapabilities, String[] requiredMaterials,
            String[] requiredEntityTypes, ListenerFactory factory) {
        this.configKey = configKey;
        this.minimumVersion = minimumVersion;
        this.requiredCapabilities = requiredCapabilities.length == 0
                ? EnumSet.noneOf(ServerCapabilities.Capability.class)
                : EnumSet.copyOf(Arrays.asList(requiredCapabilities));
        this.requiredMaterials = requiredMaterials.clone();
        this.requiredEntityTypes = requiredEntityTypes.clone();
        this.factory = factory;
    }

    public String getConfigKey() {
        return configKey;
    }

    public MinecraftVersion getMinimumVersion() {
        return minimumVersion;
    }

    public Listener createListener(List<String> worlds, ServerCapabilities capabilities) {
        return factory.create(Collections.unmodifiableList(worlds), capabilities);
    }

    public String getUnsupportedReason(MinecraftVersion detectedVersion, ServerCapabilities capabilities) {
        if (!detectedVersion.isAtLeast(minimumVersion)) {
            return "requires Minecraft " + minimumVersion + "+; detected " + detectedVersion;
        }
        for (ServerCapabilities.Capability capability : requiredCapabilities) {
            if (!capabilities.has(capability)) {
                return "runtime API is missing capability " + capability.name();
            }
        }
        if (requiredMaterials.length > 0 && !capabilities.hasMaterial(requiredMaterials)) {
            return "runtime API is missing material " + join(requiredMaterials);
        }
        if (requiredEntityTypes.length > 0 && !capabilities.hasEntityType(requiredEntityTypes)) {
            return "runtime API is missing entity type " + join(requiredEntityTypes);
        }
        return null;
    }

    private String join(String[] values) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                result.append('/');
            }
            result.append(values[i]);
        }
        return result.toString();
    }
}
