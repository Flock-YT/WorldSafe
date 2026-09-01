package me.lele.worldSafe.listener.blocks.explosionprevention;

import me.lele.worldSafe.compat.BlockExplosionSourceResolver;
import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class RespawnAnchorExplosionPreventionListener extends WorldScopedFeature {

    private final BlockExplosionSourceResolver sourceResolver = new BlockExplosionSourceResolver();

    public RespawnAnchorExplosionPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
        if (block.getBlockData() instanceof RespawnAnchor anchor
                && anchor.getCharges() > 0
                && (event.getMaterial() != Material.GLOWSTONE || anchor.getCharges() >= anchor.getMaximumCharges())) {
            sourceResolver.remember(block);
        }
    }

    @EventHandler
    public void onRespawnAnchorExplosion(BlockExplodeEvent event) {
        if (!isWorldEnabled(getWorld(event.getBlock()))) {
            return;
        }
        if (sourceResolver.isSource(event, "RESPAWN_ANCHOR")) {
            event.blockList().clear();
        }
    }
}
