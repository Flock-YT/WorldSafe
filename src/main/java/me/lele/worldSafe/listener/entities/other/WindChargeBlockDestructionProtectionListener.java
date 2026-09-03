package me.lele.worldSafe.listener.entities.other;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class WindChargeBlockDestructionProtectionListener extends WorldScopedFeature {

    public WindChargeBlockDestructionProtectionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWindChargeExplosion(EntityExplodeEvent event) {
        if (!isWindCharge(event.getEntity())) {
            return;
        }
        if (!isWorldEnabled(event.getLocation())) {
            return;
        }
        event.blockList().removeIf(this::isFragileBlock);
    }

    protected boolean isWindCharge(Entity entity) {
        return EntityTypeMatcher.matches(entity, "WIND_CHARGE", "BREEZE_WIND_CHARGE");
    }

    protected boolean isFragileBlock(Block block) {
        return MaterialMatcher.matches(block, "DECORATED_POT", "CHORUS_FLOWER", "POINTED_DRIPSTONE");
    }
}
