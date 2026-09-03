package me.lele.worldSafe.listener.blocks.explosionprevention;

import me.lele.worldSafe.compat.BlockExplosionSourceResolver;
import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class BedExplosionProtectionListener extends WorldScopedFeature {

    private final BlockExplosionSourceResolver sourceResolver;

    public BedExplosionProtectionListener(List<String> worlds) {
        this(worlds, ServerCapabilities.detect());
    }

    public BedExplosionProtectionListener(List<String> worlds, ServerCapabilities capabilities) {
        super(worlds);
        this.sourceResolver = new BlockExplosionSourceResolver(capabilities);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (!MaterialMatcher.isBed(block) || !isWorldEnabled(getWorld(block))) {
            return;
        }
        if (block.getWorld().getEnvironment() != World.Environment.NORMAL) {
            sourceResolver.remember(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBedExplosion(BlockExplodeEvent event) {
        if (!isWorldEnabled(getWorld(event.getBlock()))) {
            return;
        }
        if (sourceResolver.isSource(event, bedMaterialAliases())) {
            event.blockList().clear();
        }
    }

    private String[] bedMaterialAliases() {
        return new String[] {
                "WHITE_BED", "ORANGE_BED", "MAGENTA_BED", "LIGHT_BLUE_BED",
                "YELLOW_BED", "LIME_BED", "PINK_BED", "GRAY_BED",
                "LIGHT_GRAY_BED", "CYAN_BED", "PURPLE_BED", "BLUE_BED",
                "BROWN_BED", "GREEN_BED", "RED_BED", "BLACK_BED",
                "BED_BLOCK", "LEGACY_BED_BLOCK", "LEGACY_BED"
        };
    }
}
