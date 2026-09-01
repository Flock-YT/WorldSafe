package me.lele.worldSafe.listener.entities.explosionprevention;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class SulfurCubeExplosionProtectionListener extends WorldScopedFeature {

    public SulfurCubeExplosionProtectionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onSulfurCubeExplosion(EntityExplodeEvent event) {
        if (!isSulfurCube(event.getEntity())) {
            return;
        }
        if (isWorldEnabled(event.getLocation())) {
            event.blockList().clear();
        }
    }

    protected boolean isSulfurCube(Entity entity) {
        return EntityTypeMatcher.matches(entity, "SULFUR_CUBE", "SULPHUR_CUBE");
    }
}
