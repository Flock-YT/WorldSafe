package me.lele.worldSafe.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConfigManager {

    private final File configFile;
    private final Logger logger;
    private WorldSafeConfig config;

    public ConfigManager(File configFile, Logger logger) {
        this.configFile = configFile;
        this.logger = logger;
    }

    public WorldSafeConfig loadInitial(Iterable<String> featureKeys) {
        WorldSafeConfig loaded = loadCandidate(featureKeys);
        if (loaded != null) {
            config = loaded;
        }
        return loaded;
    }

    public WorldSafeConfig loadCandidate(Iterable<String> featureKeys) {
        YamlConfiguration yaml = new YamlConfiguration();
        try {
            yaml.load(configFile);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to read config.yml", e);
            return null;
        } catch (InvalidConfigurationException e) {
            logger.log(Level.SEVERE, "Invalid YAML in config.yml", e);
            return null;
        }

        List<String> registeredFeatureKeys = new ArrayList<String>();
        Set<String> allowedKeys = new LinkedHashSet<String>();
        allowedKeys.add("enabled");
        allowedKeys.add("enabled-bstats");
        for (String featureKey : featureKeys) {
            registeredFeatureKeys.add(featureKey);
            allowedKeys.add(featureKey);
        }

        Set<String> configuredKeys = yaml.getKeys(false);
        for (String configuredKey : configuredKeys) {
            if (!allowedKeys.contains(configuredKey)) {
                logger.severe("Unknown configuration node '" + configuredKey + "'.");
                return null;
            }
        }

        Boolean enabled = readBoolean(yaml, configuredKeys, "enabled", true);
        Boolean bStatsEnabled = readBoolean(yaml, configuredKeys, "enabled-bstats", true);
        if (enabled == null || bStatsEnabled == null) {
            return null;
        }

        Map<String, List<String>> featureWorlds = new LinkedHashMap<String, List<String>>();
        for (String featureKey : registeredFeatureKeys) {
            Object rawValue = yaml.get(featureKey);
            if (configuredKeys.contains(featureKey) && !(rawValue instanceof List<?>)) {
                logger.severe("Configuration node '" + featureKey + "' must be a list of world names.");
                return null;
            }

            List<String> worlds = new ArrayList<String>();
            Set<String> normalizedWorlds = new LinkedHashSet<String>();
            if (rawValue instanceof List<?>) {
                for (Object value : (List<?>) rawValue) {
                    if (!(value instanceof String)) {
                        logger.severe("Configuration node '" + featureKey + "' contains a non-string world name.");
                        return null;
                    }
                    String world = ((String) value).trim();
                    if (world.isEmpty()) {
                        logger.severe("Configuration node '" + featureKey + "' contains a blank world name.");
                        return null;
                    }
                    String normalized = world.toLowerCase(Locale.ROOT);
                    if (!normalizedWorlds.add(normalized)) {
                        logger.warning("Configuration node '" + featureKey + "' contains duplicate world '"
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

    private Boolean readBoolean(YamlConfiguration yaml, Set<String> configuredKeys, String key,
            boolean defaultValue) {
        if (!configuredKeys.contains(key)) {
            return Boolean.valueOf(defaultValue);
        }
        Object value = yaml.get(key);
        if (!(value instanceof Boolean)) {
            logger.severe("Configuration node '" + key + "' must be a boolean.");
            return null;
        }
        return (Boolean) value;
    }

    public void commit(WorldSafeConfig candidate) {
        config = candidate;
    }

    public WorldSafeConfig getConfig() {
        return config;
    }
}
