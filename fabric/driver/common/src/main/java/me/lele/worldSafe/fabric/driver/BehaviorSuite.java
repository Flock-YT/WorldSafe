package me.lele.worldSafe.fabric.driver;

import com.google.gson.GsonBuilder;
import me.lele.worldSafe.compat.MinecraftVersion;
import me.lele.worldSafe.core.FeatureCatalog;
import me.lele.worldSafe.core.FeatureSpec;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Outcomes are measured in a running vanilla world, never inferred from hook registration. */
public final class BehaviorSuite {
    private static final Set<Object> COMPLETED = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<Object, Boolean>());

    public static synchronized boolean completed(Object server) { return COMPLETED.contains(server); }
    private final NativeFixture fixture;
    private final List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    private final Set<String> tested = new LinkedHashSet<String>();
    private final Path config = FabricLoader.getInstance().getConfigDir().resolve("worldsafe/config.yml");

    private BehaviorSuite(NativeFixture fixture) { this.fixture = fixture; }

    public static void run(NativeFixture fixture) {
        new BehaviorSuite(fixture).run();
    }

    private void run() {
        byte[] original = null;
        try {
            original = Files.readAllBytes(config);
            for (Map.Entry<String, Boolean> permission : fixture.managementChecks().entrySet()) {
                check("permissions", permission.getKey(), () -> {
                    if (!permission.getValue()) { throw new AssertionError("Incorrect native management permission"); }
                });
            }
            for (Map.Entry<String, String> entry : fixture.explosionFeatures().entrySet()) {
                final String key = entry.getKey();
                final String source = entry.getValue();
                final Object world = "bed".equals(source) ? fixture.secondaryWorld() : fixture.primaryWorld();
                final String dimension = "bed".equals(source) ? "minecraft:the_nether" : "minecraft:overworld";
                final String alias = "bed".equals(source) ? "world_nether" : "world";
                check(key, "disabled", () -> {
                    configure("enabled: true\nenabled-bstats: false\n");
                    expectNormal(fixture.blast(world, source));
                });
                check(key, "enabled", () -> {
                    configure(configuration(key, dimension));
                    expectProtected(fixture.blast(world, source), key.endsWith("Cancel"));
                });
                check(key, "other-dimension", () -> {
                    configure(configuration(key, "bed".equals(source) ? "minecraft:overworld" : "minecraft:the_nether"));
                    expectNormal(fixture.blast(world, source));
                });
                check(key, "legacy-world-alias", () -> {
                    configure(configuration(key, alias));
                    expectProtected(fixture.blast(world, source), key.endsWith("Cancel"));
                });
                tested.add(key);
            }
            if (fixture.hasWindCharges()) {
                final String key = "breezeWindChargeImpactCancel";
                check(key, "disabled-positive-control", () -> {
                    configure("enabled: true\nenabled-bstats: false\n");
                    expectWindImpact(fixture.windImpact(fixture.primaryWorld(), true));
                });
                check(key, "enabled", () -> {
                    configure(configuration(key, "minecraft:overworld"));
                    expectProtected(fixture.windImpact(fixture.primaryWorld(), true), true);
                });
                check(key, "unconfigured-dimension-positive-control", () -> {
                    configure(configuration(key, "minecraft:the_nether"));
                    expectWindImpact(fixture.windImpact(fixture.primaryWorld(), true));
                });
                check(key, "player-wind-charge-unaffected", () -> {
                    configure(configuration(key, "minecraft:overworld"));
                    expectWindImpact(fixture.windImpact(fixture.primaryWorld(), false));
                });
                tested.add(key);
            }
            for (String key : fixture.mutationFeatures()) {
                check(key, "disabled-positive-control", () -> {
                    configure("enabled: true\nenabled-bstats: false\n");
                    if (!fixture.mutation(fixture.primaryWorld(), key)) { throw new AssertionError("Vanilla mutation did not occur"); }
                });
                check(key, "enabled", () -> {
                    configure(configuration(key, "minecraft:overworld"));
                    if (fixture.mutation(fixture.primaryWorld(), key)) { throw new AssertionError("Protected mutation occurred"); }
                });
                check(key, "unconfigured-dimension-positive-control", () -> {
                    configure(configuration(key, "minecraft:the_nether"));
                    if (!fixture.mutation(fixture.primaryWorld(), key)) { throw new AssertionError("Unconfigured dimension did not retain vanilla behavior"); }
                });
                tested.add(key);
            }
            for (String key : Arrays.asList("tntExplosionCancel", "tntExplosionProtection", "witherExplosionCancel", "witherExplosionProtection")) {
                final String source = key.startsWith("tnt") ? "minecart" : "skull";
                check(key, source + "-disabled", () -> {
                    configure("enabled: true\nenabled-bstats: false\n");
                    expectNormal(fixture.blast(fixture.primaryWorld(), source));
                });
                check(key, source + "-enabled", () -> {
                    configure(configuration(key, "minecraft:overworld"));
                    expectProtected(fixture.blast(fixture.primaryWorld(), source), key.endsWith("Cancel"));
                });
                check(key, source + "-unconfigured", () -> {
                    configure(configuration(key, "minecraft:the_nether"));
                    expectNormal(fixture.blast(fixture.primaryWorld(), source));
                });
            }
            mutationVariant("witherExplosionProtection", "wither-body", "body-destruction");
            check("tntExplosionProtection", "main-switch-off", () -> {
                configure(configuration("tntExplosionProtection", "minecraft:overworld").replace("enabled: true", "enabled: false"));
                expectNormal(fixture.blast(fixture.primaryWorld(), "tnt"));
            });
            check("tntExplosionCancel", "cancellation-precedence", () -> {
                configure(configuration("tntExplosionCancel", "minecraft:overworld")
                        + "tntExplosionProtection:\n  - minecraft:overworld\n");
                expectProtected(fixture.blast(fixture.primaryWorld(), "tnt"), true);
            });
            check("reload", "invalid-reload-retains-snapshot", () -> {
                configure(configuration("tntExplosionProtection", "minecraft:overworld"));
                Files.write(config, "enabled: [\n".getBytes(StandardCharsets.UTF_8));
                if (fixture.command("worldsafe reload") != 0) { throw new AssertionError("Invalid reload succeeded"); }
                expectProtected(fixture.blast(fixture.primaryWorld(), "tnt"), false);
            });
            check("reload", "successful-reload-disables-protection", () -> {
                configure(configuration("tntExplosionProtection", "minecraft:overworld"));
                configure("enabled: true\nenabled-bstats: false\n");
                expectNormal(fixture.blast(fixture.primaryWorld(), "tnt"));
            });
        } catch (Throwable error) {
            record("suite", "initialization", error);
        } finally {
            if (original != null) {
                try { Files.write(config, original); fixture.command("worldsafe reload"); }
                catch (Throwable error) { record("suite", "restore-configuration", error); }
            }
            writeReport();
            synchronized (BehaviorSuite.class) { COMPLETED.add(fixture.server()); }
        }
    }

    private static String configuration(String key, String dimension) {
        return "enabled: true\nenabled-bstats: false\n" + key + ":\n  - " + dimension + "\n";
    }

    private void mutationVariant(String key, String variant, String label) {
        check(key, label + "-disabled", () -> {
            configure("enabled: true\nenabled-bstats: false\n");
            if (!fixture.mutation(fixture.primaryWorld(), variant)) { throw new AssertionError("Native positive control did not mutate the world"); }
        });
        check(key, label + "-enabled", () -> {
            configure(configuration(key, "minecraft:overworld"));
            if (fixture.mutation(fixture.primaryWorld(), variant)) { throw new AssertionError("Protected native mutation occurred"); }
        });
        check(key, label + "-unconfigured", () -> {
            configure(configuration(key, "minecraft:the_nether"));
            if (!fixture.mutation(fixture.primaryWorld(), variant)) { throw new AssertionError("Unconfigured world did not retain vanilla mutation"); }
        });
    }

    private void configure(String contents) throws Exception {
        Files.write(config, contents.getBytes(StandardCharsets.UTF_8));
        if (fixture.command("worldsafe reload") != 1) { throw new AssertionError("Configuration reload failed"); }
    }

    private static void expectNormal(NativeFixture.BlastObservation result) {
        if (Boolean.TRUE.equals(result.sourceRetained)) { throw new AssertionError("Explosive block was not consumed"); }
        if (result.destroyedBlocks == 0 || result.health >= result.initialHealth || result.knockback <= 0.000001) {
            throw new AssertionError("Positive control did not destroy blocks, hurt and knock back the witness: "
                    + result.destroyedBlocks + "/" + result.health + "/" + result.knockback);
        }
    }

    private static void expectWindImpact(NativeFixture.BlastObservation result) {
        if (result.health >= result.initialHealth || result.knockback <= 0.000001) {
            throw new AssertionError("Native wind impact did not hurt and knock back the witness: " + result.health + "/" + result.knockback);
        }
    }

    private static void expectProtected(NativeFixture.BlastObservation result, boolean cancel) {
        if (result.destroyedBlocks != 0) { throw new AssertionError("Protected blocks were destroyed: " + result.destroyedBlocks); }
        if (cancel) {
            if (Boolean.FALSE.equals(result.sourceRetained)) { throw new AssertionError("Cancelled explosive block was consumed"); }
            if (result.health != result.initialHealth || result.knockback > 0.000001) { throw new AssertionError("Cancelled explosion retained damage or knockback: " + result.initialHealth + "/" + result.health + "/" + result.knockback); }
        } else if (Boolean.TRUE.equals(result.sourceRetained) || result.health >= result.initialHealth || result.knockback <= 0.000001) {
            throw new AssertionError("Block protection also removed damage or knockback");
        }
    }

    private interface Case { void run() throws Exception; }

    private void check(String feature, String scenario, Case test) {
        try { test.run(); record(feature, scenario, null); }
        catch (Throwable error) { record(feature, scenario, error); }
    }

    private void record(String feature, String scenario, Throwable error) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("feature", feature);
        result.put("scenario", scenario);
        result.put("passed", error == null);
        if (error != null) { result.put("error", error.toString()); error.printStackTrace(); }
        results.add(result);
        System.out.println("WORLDSAFE_BEHAVIOR feature=" + feature + " scenario=" + scenario + " passed=" + (error == null));
    }

    private void writeReport() {
        Map<String, Object> report = new LinkedHashMap<String, Object>();
        report.put("schemaVersion", 2);
        report.put("minecraft", fixture.game());
        report.put("stage", "native-behavior");
        report.put("results", results);
        boolean passed = !results.isEmpty();
        for (Map<String, Object> result : results) { passed &= Boolean.TRUE.equals(result.get("passed")); }
        report.put("smokePassed", passed);
        List<String> untested = new ArrayList<String>();
        MinecraftVersion game = MinecraftVersion.parse(fixture.game());
        for (FeatureSpec feature : FeatureCatalog.all().values()) {
            if (game.isAtLeast(feature.getMinimumVersion()) && !tested.contains(feature.getKey())) { untested.add(feature.getKey()); }
        }
        report.put("untestedFeatures", untested);
        Map<String, List<String>> required = new LinkedHashMap<String, List<String>>();
        required.put("tntExplosionCancel", Arrays.asList("minecart-disabled", "minecart-enabled", "minecart-unconfigured"));
        required.put("tntExplosionProtection", Arrays.asList("minecart-disabled", "minecart-enabled", "minecart-unconfigured"));
        required.put("witherExplosionCancel", Arrays.asList("skull-disabled", "skull-enabled", "skull-unconfigured"));
        required.put("witherExplosionProtection", Arrays.asList("skull-disabled", "skull-enabled", "skull-unconfigured", "body-destruction-disabled", "body-destruction-enabled", "body-destruction-unconfigured"));
        required.put("ghastExplosionCancel", Arrays.asList("direct-hit-disabled", "direct-hit-enabled", "non-ghast-unaffected"));
        required.put("fireSpreadPrevention", Arrays.asList("lava-disabled", "lava-enabled", "lava-unconfigured", "natural-extinguish-unaffected"));
        required.put("fireIgnitionPrevention", Arrays.asList("fireball-disabled", "fireball-enabled", "crystal-disabled", "crystal-enabled", "arrow-disabled", "arrow-enabled", "explosion-disabled", "explosion-enabled", "player-ignition-unaffected"));
        required.put("windChargeBlockDestructionProtection", Arrays.asList("player-pot", "breeze-pot", "player-chorus", "breeze-chorus", "player-dripstone", "breeze-dripstone", "damage-and-knockback-retained"));
        required.put("witherRoseFormationPrevention", Arrays.asList("item-fallback-disabled", "item-fallback-enabled", "ordinary-drops-retained"));
        required.put("villagerCropModificationProtection", Arrays.asList("planting-disabled", "planting-enabled", "planting-unconfigured"));
        required.put("silverfishBlockChangeProtection", Arrays.asList("infestation-disabled", "infestation-enabled", "infestation-unconfigured"));
        List<String> untestedMechanisms = new ArrayList<String>();
        for (Map.Entry<String, List<String>> feature : required.entrySet()) {
            if (!game.isAtLeast(FeatureCatalog.get(feature.getKey()).getMinimumVersion())) { continue; }
            for (String scenario : feature.getValue()) {
                boolean found = false;
                for (Map<String, Object> result : results) {
                    found |= feature.getKey().equals(result.get("feature")) && scenario.equals(result.get("scenario"));
                }
                if (!found) { untestedMechanisms.add(feature.getKey() + "/" + scenario); }
            }
        }
        report.put("untestedMechanisms", untestedMechanisms);
        report.put("featureKeysCovered", tested);
        Map<String, Object> features = new LinkedHashMap<String, Object>();
        for (FeatureSpec feature : FeatureCatalog.all().values()) {
            Map<String, Object> status = new LinkedHashMap<String, Object>();
            boolean applicable = game.isAtLeast(feature.getMinimumVersion());
            List<String> scenarios = new ArrayList<String>();
            boolean failed = false;
            for (Map<String, Object> result : results) {
                if (feature.getKey().equals(result.get("feature"))) {
                    scenarios.add(String.valueOf(result.get("scenario")));
                    failed |= !Boolean.TRUE.equals(result.get("passed"));
                }
            }
            boolean partial = false;
            for (String missing : untestedMechanisms) { partial |= missing.startsWith(feature.getKey() + "/"); }
            status.put("minimumVersion", feature.getMinimumVersion().toString());
            status.put("applicable", applicable);
            status.put("status", !applicable ? "NOT_APPLICABLE" : !tested.contains(feature.getKey()) ? "UNTESTED"
                    : failed ? "FAILED" : partial ? "PARTIAL" : "PASSED");
            status.put("scenarios", scenarios);
            features.put(feature.getKey(), status);
        }
        report.put("features", features);
        boolean complete = passed && untested.isEmpty() && untestedMechanisms.isEmpty();
        report.put("complete", complete);
        try {
            Path file = FabricLoader.getInstance().getGameDir().resolve("worldsafe-behavior.json");
            Files.write(file, new GsonBuilder().setPrettyPrinting().create().toJson(report).getBytes(StandardCharsets.UTF_8));
            System.out.println("WORLDSAFE_BEHAVIOR completed smokePassed=" + passed + " complete=" + complete);
        } catch (Exception error) { throw new IllegalStateException("Cannot write behavior report", error); }
    }
}
