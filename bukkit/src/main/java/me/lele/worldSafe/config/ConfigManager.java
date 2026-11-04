package me.lele.worldSafe.config;

import me.lele.worldSafe.core.ConfigValidator;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
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

        return ConfigValidator.parse(yaml.getValues(false), featureKeys, logger::severe, logger::warning);
    }

    public void commit(WorldSafeConfig candidate) {
        config = candidate;
    }

    public WorldSafeConfig getConfig() {
        return config;
    }
}
