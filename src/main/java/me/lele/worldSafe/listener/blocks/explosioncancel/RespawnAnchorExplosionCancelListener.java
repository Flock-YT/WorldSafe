package me.lele.worldSafe.listener.blocks.explosioncancel;

import me.lele.worldSafe.compat.BlockExplosionSourceResolver;
import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class RespawnAnchorExplosionCancelListener extends WorldScopedFeature {

    private final ServerCapabilities capabilities;
    private final BlockExplosionSourceResolver sourceResolver;

    public RespawnAnchorExplosionCancelListener(List<String> worlds) {
        this(worlds, ServerCapabilities.detect());
    }

    public RespawnAnchorExplosionCancelListener(List<String> worlds, ServerCapabilities capabilities) {
        super(worlds);
        this.capabilities = capabilities;
        this.sourceResolver = new BlockExplosionSourceResolver(capabilities);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAnchorUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (!MaterialMatcher.matches(block, "RESPAWN_ANCHOR") || !isWorldEnabled(getWorld(block))) {
            return;
        }
        if (block.getWorld().getEnvironment() == World.Environment.NETHER) {
            return;
        }
        int charges = capabilities.getRespawnAnchorCharges(block);
        int maximumCharges = capabilities.getRespawnAnchorMaximumCharges(block);
        if (charges > 0 && (event.getMaterial() != Material.GLOWSTONE || charges >= maximumCharges)) {
            sourceResolver.remember(block);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onRespawnAnchorExplosion(BlockExplodeEvent event) {
        if (!isWorldEnabled(getWorld(event.getBlock()))) {
            return;
        }
        if (sourceResolver.isSource(event, "RESPAWN_ANCHOR")) {
            event.setCancelled(true);
        }
    }
}
