package me.lele.worldSafe.listener.blocks.other;

import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;

import java.util.List;

public class FireSpreadPreventionListener extends WorldScopedFeature {

        public FireSpreadPreventionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        void onFireSpread(BlockSpreadEvent event) {
                if (!isFire(event.getNewState().getType())) {
                        return;
                }
                if (!isWorldEnabled(getWorld(event.getBlock()))) {
                        return;
                }
                event.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        void onBlockIgnite(BlockIgniteEvent event) {
                if (!isWorldEnabled(getWorld(event.getBlock()))) {
                        return;
                }

                BlockIgniteEvent.IgniteCause cause = event.getCause();
                if (cause == BlockIgniteEvent.IgniteCause.SPREAD || cause == BlockIgniteEvent.IgniteCause.LAVA) {
                        event.setCancelled(true);
                }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        void onBlockBurn(BlockBurnEvent event) {
                if (!isWorldEnabled(getWorld(event.getBlock()))) {
                        return;
                }
                event.setCancelled(true);
        }

        private boolean isFire(Material material) {
                return MaterialMatcher.matches(material, "FIRE", "SOUL_FIRE");
        }
}
