package me.lele.worldSafe.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.mockito.Mockito.*;

abstract class FabricTestSupport {
    static final NativeAccess ACCESS = new NativeAccess() {
        public Object server(Object input) { return ((World) input).server; }
        public String dimension(Object input) { return ((World) input).dimension; }
        public String entityType(Object input) { return input instanceof Entity ? ((Entity) input).type : ""; }
        public Object projectileOwner(Object input) { return input instanceof Entity ? ((Entity) input).owner : null; }
        public String blockType(Object input, Object pos) { return ((World) input).blocks.getOrDefault(pos, "minecraft:air"); }
        public String stateType(Object input) { return input == null ? "minecraft:air" : (String) input; }
        public boolean isWitherRoseItem(Object input) { return false; }
        public boolean isProjectile(Object input) { return input instanceof Entity && ((Entity) input).projectile; }
        public Object commandServer(Object input) { return ((Source) input).server; }
        public boolean mayManage(Object input) { return ((Source) input).allowed; }
        public void reply(Object input, String message) { ((Source) input).messages.add(message); }
    };
    static { FabricServices.install(ACCESS); }

    @TempDir Path directory;
    private MockedStatic<FabricLoader> mockedLoader;
    private final List<Object> servers = new ArrayList<Object>();
    FabricLoader loader;
    ModMetadata metadata;
    final Object server = new Object();
    final World overworld = new World(server, "minecraft:overworld");
    final World nether = new World(server, "minecraft:the_nether");
    final Object position = new Object();

    @BeforeEach void initializeLoader() throws Exception {
        loader = mock(FabricLoader.class);
        metadata = mock(ModMetadata.class);
        ModContainer minecraft = mock(ModContainer.class);
        when(minecraft.getMetadata()).thenReturn(metadata);
        when(metadata.getVersion()).thenReturn(Version.parse("26.3"));
        when(loader.getConfigDir()).thenReturn(directory);
        when(loader.getModContainer("minecraft")).thenReturn(Optional.of(minecraft));
        mockedLoader = mockStatic(FabricLoader.class);
        mockedLoader.when(FabricLoader::getInstance).thenReturn(loader);
        servers.add(server);
    }

    @AfterEach void cleanup() {
        for (Object instance : servers) { FabricServices.stop(instance); }
        ExplosionPolicy.restoreEntitySource(null);
        ExplosionPolicy.clearBlockSource();
        BlockPolicy.restoreActor(null);
        BlockPolicy.restoreReason(null);
        mockedLoader.close();
    }

    void write(String yaml) throws Exception {
        Path path = directory.resolve("worldsafe/config.yml");
        Files.createDirectories(path.getParent());
        Files.write(path, yaml.getBytes(StandardCharsets.UTF_8));
    }

    void configure(String... keys) throws Exception {
        StringBuilder yaml = new StringBuilder("enabled: true\nenabled-bstats: false\n");
        for (String key : keys) { yaml.append(key).append(":\n  - minecraft:overworld\n"); }
        write(yaml.toString());
        FabricServices.start(server);
    }

    World anotherServer() {
        Object instance = new Object();
        servers.add(instance);
        return new World(instance, "minecraft:overworld");
    }

    Entity entity(String type) { return new Entity(overworld, "minecraft:" + type); }

    static final class World {
        final Object server;
        final String dimension;
        final Map<Object, String> blocks = new HashMap<Object, String>();
        World(Object server, String dimension) { this.server = server; this.dimension = dimension; }
    }

    static final class Entity implements NativeEntityAccess {
        final World world;
        final String type;
        Entity owner;
        boolean projectile;
        Entity(World world, String type) { this.world = world; this.type = type; }
        public Object worldsafe$getWorld() { return world; }
        Entity projectile(Entity owner) { this.projectile = true; this.owner = owner; return this; }
    }

    static final class Source {
        final Object server;
        final boolean allowed;
        final List<String> messages = new ArrayList<String>();
        Source(Object server, boolean allowed) { this.server = server; this.allowed = allowed; }
    }
}
