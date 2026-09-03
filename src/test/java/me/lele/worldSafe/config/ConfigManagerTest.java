package me.lele.worldSafe.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void invalidSemanticReloadCandidateKeepsCommittedConfiguration() throws Exception {
        File file = writeConfig("enabled: true", "testFeature:", "  - world");
        ConfigManager manager = manager(file);
        WorldSafeConfig original = manager.loadInitial(CollectionsHelper.singleton("testFeature"));
        assertNotNull(original);

        Files.write(file.toPath(), Arrays.asList("testFeature:", "  - '   '"), StandardCharsets.UTF_8);
        assertNull(manager.loadCandidate(CollectionsHelper.singleton("testFeature")));
        assertSame(original, manager.getConfig());
    }

    @Test
    void rejectsUnknownTopLevelKeys() throws Exception {
        File file = writeConfig("enabled: true", "testFeature: []", "testFeatre: []");
        assertNull(manager(file).loadCandidate(CollectionsHelper.singleton("testFeature")));
    }

    @Test
    void rejectsNonBooleanSwitches() throws Exception {
        File enabledFile = writeConfig("enabled: 'true'", "testFeature: []");
        assertNull(manager(enabledFile).loadCandidate(CollectionsHelper.singleton("testFeature")));

        File bStatsFile = writeConfig("enabled-bstats: 1", "testFeature: []");
        assertNull(manager(bStatsFile).loadCandidate(CollectionsHelper.singleton("testFeature")));
    }

    @Test
    void rejectsInvalidWorldNames() throws Exception {
        File nonStringFile = writeConfig("testFeature:", "  - world", "  - 42");
        assertNull(manager(nonStringFile).loadCandidate(CollectionsHelper.singleton("testFeature")));

        File blankFile = writeConfig("testFeature:", "  - '   '");
        assertNull(manager(blankFile).loadCandidate(CollectionsHelper.singleton("testFeature")));
    }

    @Test
    void trimsWorldNamesAndKeepsFirstCaseInsensitiveDuplicate() throws Exception {
        File file = writeConfig("testFeature:", "  - ' World '", "  - world", "  - Nether");
        CapturingHandler handler = new CapturingHandler();
        Logger logger = logger();
        logger.addHandler(handler);

        WorldSafeConfig config = new ConfigManager(file, logger)
                .loadCandidate(CollectionsHelper.singleton("testFeature"));

        assertNotNull(config);
        assertEquals(Arrays.asList("World", "Nether"), config.getWorlds("testFeature"));
        assertTrue(handler.contains("duplicate world 'world'"));
    }

    private File writeConfig(String... lines) throws Exception {
        File file = temporaryDirectory.resolve("config-" + System.nanoTime() + ".yml").toFile();
        Files.write(file.toPath(), Arrays.asList(lines), StandardCharsets.UTF_8);
        return file;
    }

    private ConfigManager manager(File file) {
        return new ConfigManager(file, logger());
    }

    private Logger logger() {
        Logger logger = Logger.getLogger("ConfigManagerTest." + System.nanoTime());
        logger.setUseParentHandlers(false);
        return logger;
    }

    private static final class CapturingHandler extends Handler {
        private final List<String> messages = new ArrayList<String>();

        @Override
        public void publish(LogRecord record) {
            messages.add(record.getMessage());
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }

        private boolean contains(String text) {
            for (String message : messages) {
                if (message.contains(text)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static final class CollectionsHelper {
        private static <T> Iterable<T> singleton(T value) {
            return java.util.Collections.singleton(value);
        }
    }
}
