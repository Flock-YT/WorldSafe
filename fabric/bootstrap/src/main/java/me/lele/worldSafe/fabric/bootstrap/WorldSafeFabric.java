package me.lele.worldSafe.fabric.bootstrap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

/** Java 8 bootstrap: deliberately contains no references to Minecraft classes. */
public final class WorldSafeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ModContainer runtime = FabricLoader.getInstance().getModContainer("worldsafe_runtime")
                .orElseThrow(() -> new IllegalStateException("WorldSafe runtime was not selected"));
        String selected = runtime.getMetadata().getCustomValue("worldsafe:game").getAsString();
        String actual = FabricLoader.getInstance().getModContainer("minecraft").get()
                .getMetadata().getVersion().getFriendlyString();
        if (!selected.equals(actual)) {
            throw new IllegalStateException("WorldSafe runtime mismatch: " + selected + " / " + actual);
        }
        if (Boolean.getBoolean("worldsafe.diagnostics")) System.out.println("WORLDSAFE_ARCHITECTURE selected=" + selected
                + " environment=" + FabricLoader.getInstance().getEnvironmentType()
                + " java=" + System.getProperty("java.version"));
    }
}
