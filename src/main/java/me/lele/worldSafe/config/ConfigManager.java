package me.lele.worldSafe.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

        Map<String, List<String>> featureWorlds = new LinkedHashMap<String, List<String>>();
        for (String featureKey : featureKeys) {
            Object rawValue = yaml.get(featureKey);
            if (rawValue != null && !(rawValue instanceof List<?>)) {
                logger.severe("Configuration node '" + featureKey + "' must be a list of world names.");
                return null;
            }
            List<String> worlds = yaml.getStringList(featureKey);
            if (rawValue instanceof List<?> && worlds.size() != ((List<?>) rawValue).size()) {
                logger.severe("Configuration node '" + featureKey + "' contains a non-string world name.");
                return null;
            }
            featureWorlds.put(featureKey, worlds);
        }

        return new WorldSafeConfig(yaml.getBoolean("enabled", true),
                yaml.getBoolean("enabled-bstats", true), featureWorlds);
    }

    public void commit(WorldSafeConfig candidate) {
        config = candidate;
    }

    public WorldSafeConfig getConfig() {
        return config;
    }
}
