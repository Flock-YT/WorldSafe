package me.lele.worldSafe.fabric;

import me.lele.worldSafe.core.ProtectionPolicy;
import net.fabricmc.loader.api.Version;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FabricServicesTest extends FabricTestSupport {
    @Test void firstRunCreatesOnlyTheTwoDefaultProtections() throws Exception {
        FabricServices.start(server);
        assertTrue(FabricServices.enabled("tntExplosionProtection", overworld));
        assertTrue(FabricServices.enabled("creeperExplosionProtection", nether));
        assertFalse(FabricServices.enabled("tntExplosionCancel", overworld));
        String yaml = new String(Files.readAllBytes(directory.resolve("worldsafe/config.yml")), StandardCharsets.UTF_8);
        assertTrue(yaml.contains("enabled-bstats: false"));
        assertTrue(yaml.contains("minecraft:the_end"));
    }

    @Test void malformedInitialConfigurationAbortsWithoutInstallingState() throws Exception {
        write("enabled: [\n");
        assertThrows(RuntimeException.class, () -> FabricServices.start(server));
        assertNull(FabricServices.policy(overworld));
    }

    @Test void duplicateYamlKeysAreRejectedAtStartup() throws Exception {
        write("enabled: true\nenabled: false\n");
        assertThrows(RuntimeException.class, () -> FabricServices.start(server));
        assertNull(FabricServices.policy(overworld));
    }

    @Test void failedReloadPreservesTheExactSnapshotThenValidReloadReplacesIt() throws Exception {
        configure("tntExplosionProtection");
        ProtectionPolicy previous = FabricServices.policy(overworld);
        Source console = new Source(server, true);
        write("enabled: [\n");
        assertEquals(0, FabricServices.command(console, true));
        assertSame(previous, FabricServices.policy(overworld));
        write("enabled: false\nenabled-bstats: false\n");
        assertEquals(1, FabricServices.command(console, true));
        assertNotSame(previous, FabricServices.policy(overworld));
        assertFalse(FabricServices.enabled("tntExplosionProtection", overworld));
    }

    @Test void unknownKeysAndIncorrectListsCannotReplaceAWorkingSnapshot() throws Exception {
        configure("tntExplosionProtection");
        ProtectionPolicy previous = FabricServices.policy(overworld);
        for (String invalid : new String[] {"typo: []\n", "tntExplosionProtection: true\n", "tntExplosionProtection: [42]\n",
                "tntExplosionProtection: [missing_namespace]\n"}) {
            write(invalid);
            assertEquals(0, FabricServices.command(new Source(server, true), true));
            assertSame(previous, FabricServices.policy(overworld));
        }
    }

    @Test void deniedCommandsCannotReadOrReplaceConfiguration() throws Exception {
        configure("tntExplosionProtection");
        ProtectionPolicy previous = FabricServices.policy(overworld);
        write("enabled: false\n");
        Source visitor = new Source(server, false);
        assertEquals(0, FabricServices.command(visitor, false));
        assertEquals(0, FabricServices.command(visitor, true));
        assertSame(previous, FabricServices.policy(overworld));
        assertEquals(2, visitor.messages.size());
    }

    @Test void serverInstancesHaveIndependentSnapshotsAndStoppingClearsOnlyTheirState() throws Exception {
        configure("tntExplosionProtection");
        World second = anotherServer();
        write("enabled: true\nenabled-bstats: false\ntntExplosionCancel: [world]\n");
        FabricServices.start(second.server);
        assertTrue(FabricServices.enabled("tntExplosionProtection", overworld));
        assertFalse(FabricServices.enabled("tntExplosionCancel", overworld));
        assertTrue(FabricServices.enabled("tntExplosionCancel", second));
        FabricServices.stop(server);
        assertNull(FabricServices.policy(overworld));
        assertNotNull(FabricServices.policy(second));
        FabricServices.stop(server); // Stopping an already stopped integrated server is harmless.
        World reopened = anotherServer();
        write("enabled: false\nenabled-bstats: false\n");
        FabricServices.start(reopened.server);
        assertFalse(FabricServices.enabled("tntExplosionCancel", reopened));
    }

    @Test void nativeDimensionsAndLegacyAliasesWorkAndUnavailableFeaturesStayDisabled() throws Exception {
        when(metadata.getVersion()).thenReturn(Version.parse("1.14"));
        write("enabled: true\nenabled-bstats: true\ntntExplosionProtection: [world_nether, 'example:moon']\nsulfurCubeExplosionCancel: [world]\n");
        FabricServices.start(server);
        assertTrue(FabricServices.enabled("tntExplosionProtection", nether));
        assertTrue(FabricServices.enabled("tntExplosionProtection", new World(server, "example:moon")));
        assertFalse(FabricServices.enabled("tntExplosionProtection", overworld));
        assertFalse(FabricServices.enabled("sulfurCubeExplosionCancel", overworld));
    }

    @Test void remoteClientWorldsHaveNoLocalProtectionAndRuntimeInstallationIsUnique() throws Exception {
        configure("tntExplosionCancel");
        assertFalse(FabricServices.enabled("tntExplosionCancel", new World(null, "minecraft:overworld")));
        assertThrows(IllegalStateException.class, () -> FabricServices.install(ACCESS));
        assertSame(ACCESS, FabricServices.nativeAccess());
    }
}
