package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockIgniteEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FireIgnitionPreventionListener extends WorldScopedFeature {

    private static final Set<String> PREVENTED_CAUSES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
            "FIREBALL", "LIGHTNING", "EXPLOSION", "ENDER_CRYSTAL", "END_CRYSTAL", "ARROW")));

    public FireIgnitionPreventionListener(List<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!isWorldEnabled(getWorld(event.getBlock()))) {
            return;
        }
        if (PREVENTED_CAUSES.contains(event.getCause().name())) {
            event.setCancelled(true);
        }
    }
}
