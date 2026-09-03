package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.block.Block;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.List;

public class DecoratedPotProjectileProtectionListener extends WorldScopedFeature {

    private final ServerCapabilities capabilities;

    public DecoratedPotProjectileProtectionListener(List<String> worlds) {
        this(worlds, ServerCapabilities.detect());
    }

    public DecoratedPotProjectileProtectionListener(List<String> worlds, ServerCapabilities capabilities) {
        super(worlds);
        this.capabilities = capabilities;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(ProjectileHitEvent event) {
        Block hitBlock = capabilities.getProjectileHitBlock(event);
        if (isDecoratedPot(hitBlock) && isWorldEnabled(getWorld(hitBlock))) {
            capabilities.cancelIfPossible(event);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileChangeBlock(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Projectile)) {
            return;
        }
        if (isDecoratedPot(event.getBlock()) && isWorldEnabled(getWorld(event.getBlock()))) {
            event.setCancelled(true);
        }
    }

    protected boolean isDecoratedPot(Block block) {
        return MaterialMatcher.matches(block, "DECORATED_POT");
    }
}
