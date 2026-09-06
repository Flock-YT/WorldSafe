package me.lele.worldSafe;

import me.lele.worldSafe.feature.FeatureDefinition;
import me.lele.worldSafe.config.ConfigManager;
import me.lele.worldSafe.config.WorldSafeConfig;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginMetadataTest {

    @Test
    void pluginDescriptionLoadsOnTheLegacyApiParser() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("plugin.yml");
        assertNotNull(input);
        PluginDescriptionFile description = new PluginDescriptionFile(input);
        assertEquals("me.lele.worldSafe.WorldSafe", description.getMain());
        assertTrue(description.getCommands().containsKey("worldsafe"));
    }

    @Test
    void minimalDefaultConfigurationOnlyEnablesExampleProtections(@TempDir Path directory) throws Exception {
        Path file = directory.resolve("config.yml");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            assertNotNull(input);
            Files.copy(input, file);
        }
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(file.toFile());
        assertEquals(new LinkedHashSet<String>(Arrays.asList("enabled", "enabled-bstats",
                "creeperExplosionProtection", "tntExplosionProtection")), configuration.getKeys(false));
        List<String> featureKeys = new ArrayList<String>();
        for (FeatureDefinition feature : WorldSafe.FEATURES) {
            featureKeys.add(feature.getConfigKey());
        }
        WorldSafeConfig loaded = new ConfigManager(file.toFile(), Logger.getLogger("PluginMetadataTest"))
                .loadInitial(featureKeys);
        assertNotNull(loaded);
        assertTrue(loaded.isEnabled());
        assertTrue(loaded.isBStatsEnabled());
        for (FeatureDefinition feature : WorldSafe.FEATURES) {
            String key = feature.getConfigKey();
            List<String> expected = key.equals("creeperExplosionProtection") || key.equals("tntExplosionProtection")
                    ? Arrays.asList("world", "world_nether", "world_the_end") : Collections.<String>emptyList();
            assertEquals(expected, loaded.getWorlds(key), key);
        }
    }
}
