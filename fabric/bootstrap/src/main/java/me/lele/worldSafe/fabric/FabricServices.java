package me.lele.worldSafe.fabric;

import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.config.WorldSafeConfig;
import me.lele.worldSafe.core.ConfigValidator;
import me.lele.worldSafe.core.FeatureCatalog;
import me.lele.worldSafe.core.ProtectionPolicy;
import me.lele.worldSafe.core.WorldSelector;
import net.fabricmc.loader.api.FabricLoader;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Server-scoped snapshots: no game objects or registries are linked by the bootstrap. */
public final class FabricServices {
    private static final Logger LOGGER = Logger.getLogger("WorldSafe");
    private static final Map<Object, ProtectionPolicy> SERVERS = new WeakHashMap<Object, ProtectionPolicy>();
    private static volatile NativeAccess access;

    private FabricServices() { }

    public static void install(NativeAccess selected) {
        if (access != null) { throw new IllegalStateException("Multiple WorldSafe runtime modules loaded"); }
        access = selected;
    }

    public static NativeAccess nativeAccess() {
        NativeAccess selected = access;
        if (selected == null) { throw new IllegalStateException("WorldSafe runtime is not initialized"); }
        return selected;
    }

    public static void start(Object server) {
        ProtectionPolicy policy = load();
        synchronized (SERVERS) { SERVERS.put(server, policy); }
        LOGGER.info("WorldSafe configuration loaded for this server instance.");
    }

    public static void stop(Object server) {
        synchronized (SERVERS) { SERVERS.remove(server); }
    }

    public static ProtectionPolicy policy(Object world) {
        Object server = nativeAccess().server(world);
        synchronized (SERVERS) { return SERVERS.get(server); }
    }

    public static boolean enabled(String key, Object world) {
        if (world == null) { return false; }
        ProtectionPolicy policy = policy(world);
        return policy != null && policy.enabled(key, nativeAccess().dimension(world));
    }

    public static int command(Object source, boolean reload) {
        NativeAccess selected = nativeAccess();
        if (!selected.mayManage(source)) {
            selected.reply(source, "You do not have permission to use this command.");
            return 0;
        }
        if (!reload) {
            selected.reply(source, "/worldsafe help - Command help; /worldsafe reload - Reload the configuration");
            return 1;
        }
        try {
            ProtectionPolicy candidate = load();
            synchronized (SERVERS) { SERVERS.put(selected.commandServer(source), candidate); }
            selected.reply(source, "WorldSafe configuration reloaded.");
            return 1;
        } catch (RuntimeException error) {
            LOGGER.log(Level.SEVERE, "Reload failed; previous configuration remains active.", error);
            selected.reply(source, "WorldSafe reload failed. The previous configuration is still active.");
            return 0;
        }
    }

    private static ProtectionPolicy load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("worldsafe/config.yml");
        try {
            Files.createDirectories(file.getParent());
            if (!Files.exists(file)) {
                String defaults = "enabled: true\nenabled-bstats: false\n"
                        + "creeperExplosionProtection:\n  - minecraft:overworld\n  - minecraft:the_nether\n  - minecraft:the_end\n"
                        + "tntExplosionProtection:\n  - minecraft:overworld\n  - minecraft:the_nether\n  - minecraft:the_end\n";
                Files.write(file, defaults.getBytes(StandardCharsets.UTF_8));
            }
            LoaderOptions options = new LoaderOptions();
            options.setAllowDuplicateKeys(false);
            Object decoded;
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                decoded = new Yaml(new SafeConstructor(options)).load(reader);
            }
            if (decoded == null) { decoded = Collections.emptyMap(); }
            if (!(decoded instanceof Map)) { throw new IllegalArgumentException("config.yml must contain a mapping"); }
            WorldSafeConfig config = ConfigValidator.parse((Map<?, ?>) decoded,
                    FeatureCatalog.all().keySet(), LOGGER::severe, LOGGER::warning);
            if (config == null) { throw new IllegalArgumentException("Invalid WorldSafe configuration"); }
            for (String feature : FeatureCatalog.all().keySet()) {
                for (String dimension : config.getWorlds(feature)) {
                    if (!WorldSelector.isFabricDimension(dimension)) {
                        throw new IllegalArgumentException("Invalid Fabric dimension '" + dimension
                                + "' in " + feature + "; use namespace:path or a legacy world alias");
                    }
                }
            }
            if (config.isBStatsEnabled()) {
                LOGGER.warning("enabled-bstats has no effect on Fabric; WorldSafe does not upload metrics.");
            }
            String game = FabricLoader.getInstance().getModContainer("minecraft").get()
                    .getMetadata().getVersion().getFriendlyString();
            return new ProtectionPolicy(config, MinecraftVersion.parse(game), WorldSelector::fabricDimension);
        } catch (IOException error) {
            throw new IllegalStateException("Unable to read " + file, error);
        }
    }
}
