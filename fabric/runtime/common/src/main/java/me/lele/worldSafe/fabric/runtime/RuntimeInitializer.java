package me.lele.worldSafe.fabric.runtime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Installs the native adapter selected by Fabric Loader. */
public final class RuntimeInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        me.lele.worldSafe.fabric.FabricServices.install(new me.lele.worldSafe.fabric.runtime.NativeAccessImpl());
        marker("runtime-initialized");
    }

    public static void marker(String stage) {
        if (!Boolean.getBoolean("worldsafe.diagnostics")) { return; }
        String game = FabricLoader.getInstance().getModContainer("worldsafe_runtime").get()
                .getMetadata().getCustomValue("worldsafe:game").getAsString();
        System.out.println("WORLDSAFE_ARCHITECTURE game=" + game + " stage=" + stage);
    }
}
