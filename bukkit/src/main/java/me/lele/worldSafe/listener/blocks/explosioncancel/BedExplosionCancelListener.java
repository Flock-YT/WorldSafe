package me.lele.worldSafe.listener.blocks.explosioncancel;

import me.lele.worldSafe.compat.BlockExplosionSourceResolver;
import me.lele.worldSafe.compat.MaterialMatcher;
import me.lele.worldSafe.compat.ServerCapabilities;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class BedExplosionCancelListener extends WorldScopedFeature {

        private final BlockExplosionSourceResolver sourceResolver;

        public BedExplosionCancelListener(List<String> worlds) {
                this(worlds, ServerCapabilities.detect());
        }

        public BedExplosionCancelListener(List<String> worlds, ServerCapabilities capabilities) {
                super(worlds);
                this.sourceResolver = new BlockExplosionSourceResolver(capabilities);
        }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		// 检查玩家是否为右键点击
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		// 检查玩家点击的是否为方块
		if (!e.hasBlock())
			return;
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock == null)
                        return;
                World w = getWorld(clickedBlock);
                if (!isWorldEnabled(w))
                        return;
                // 判断是否在主世界触发
                if (w.getEnvironment() == Environment.NORMAL)
                        return;
                // 检查玩家点击的是否为床
                if (!MaterialMatcher.isBed(clickedBlock))
                        return;
                // 取消事件，防止爆炸
                e.setCancelled(true);

        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBedExplosion(BlockExplodeEvent event) {
                if (!isWorldEnabled(getWorld(event.getBlock()))) {
                        return;
                }
                if (sourceResolver.isSource(event, bedMaterialAliases())) {
                        event.setCancelled(true);
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
