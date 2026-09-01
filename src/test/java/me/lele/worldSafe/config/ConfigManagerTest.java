package me.lele.worldSafe.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConfigManagerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void invalidReloadCandidateKeepsCommittedConfiguration() throws Exception {
        File file = temporaryDirectory.resolve("config.yml").toFile();
        Files.write(file.toPath(), Arrays.asList("enabled: true", "testFeature:", "  - world"),
                StandardCharsets.UTF_8);
        Logger logger = Logger.getLogger("ConfigManagerTest");
        logger.setUseParentHandlers(false);
        ConfigManager manager = new ConfigManager(file, logger);
        WorldSafeConfig original = manager.loadInitial(CollectionsHelper.singleton("testFeature"));
        assertNotNull(original);

        Files.write(file.toPath(), Arrays.asList("enabled: ["), StandardCharsets.UTF_8);
        assertNull(manager.loadCandidate(CollectionsHelper.singleton("testFeature")));
        assertSame(original, manager.getConfig());
    }

    private static final class CollectionsHelper {
        private static <T> Iterable<T> singleton(T value) {
            return java.util.Collections.singleton(value);
        }
    }
}
