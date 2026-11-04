package me.lele.worldSafe.core;

import me.lele.worldSafe.config.WorldSafeConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Schema validation shared by the platform-specific YAML readers. */
public final class ConfigValidator {
    private ConfigValidator() { }

    public static WorldSafeConfig parse(Map<?, ?> raw, Iterable<String> featureKeys,
            Consumer<String> errors, Consumer<String> warnings) {
        List<String> registeredFeatureKeys = new ArrayList<String>();
        Set<String> allowedKeys = new LinkedHashSet<String>();
        allowedKeys.add("enabled");
        allowedKeys.add("enabled-bstats");
        for (String featureKey : featureKeys) {
            registeredFeatureKeys.add(featureKey);
            allowedKeys.add(featureKey);
        }

        Set<?> configuredKeys = raw.keySet();
        for (Object configuredKey : configuredKeys) {
            if (!allowedKeys.contains(configuredKey)) {
                errors.accept("Unknown configuration node '" + configuredKey + "'.");
                return null;
            }
        }

        Boolean enabled = readBoolean(raw, configuredKeys, "enabled", true, errors);
        Boolean bStatsEnabled = readBoolean(raw, configuredKeys, "enabled-bstats", true, errors);
        if (enabled == null || bStatsEnabled == null) {
            return null;
        }

        Map<String, List<String>> featureWorlds = new LinkedHashMap<String, List<String>>();
        for (String featureKey : registeredFeatureKeys) {
            Object rawValue = raw.get(featureKey);
            if (configuredKeys.contains(featureKey) && !(rawValue instanceof List<?>)) {
                errors.accept("Configuration node '" + featureKey + "' must be a list of world names.");
                return null;
            }

            List<String> worlds = new ArrayList<String>();
            Set<String> normalizedWorlds = new LinkedHashSet<String>();
            if (rawValue instanceof List<?>) {
                for (Object value : (List<?>) rawValue) {
                    if (!(value instanceof String)) {
                        errors.accept("Configuration node '" + featureKey + "' contains a non-string world name.");
                        return null;
                    }
                    String world = ((String) value).trim();
                    if (world.isEmpty()) {
                        errors.accept("Configuration node '" + featureKey + "' contains a blank world name.");
                        return null;
                    }
                    String normalized = world.toLowerCase(Locale.ROOT);
                    if (!normalizedWorlds.add(normalized)) {
                        warnings.accept("Configuration node '" + featureKey + "' contains duplicate world '"
                                + world + "'; keeping the first entry.");
                        continue;
                    }
                    worlds.add(world);
                }
            }
            featureWorlds.put(featureKey, worlds);
        }

        return new WorldSafeConfig(enabled.booleanValue(), bStatsEnabled.booleanValue(), featureWorlds);
    }

    private static Boolean readBoolean(Map<?, ?> raw, Set<?> configuredKeys, String key,
            boolean defaultValue, Consumer<String> errors) {
        if (!configuredKeys.contains(key)) {
            return Boolean.valueOf(defaultValue);
        }
        Object value = raw.get(key);
        if (!(value instanceof Boolean)) {
            errors.accept("Configuration node '" + key + "' must be a boolean.");
            return null;
        }
        return (Boolean) value;
    }

}
