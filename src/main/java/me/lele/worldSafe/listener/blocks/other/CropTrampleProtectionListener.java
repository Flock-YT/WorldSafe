package me.lele.worldSafe.listener.blocks.other;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class CropTrampleProtectionListener implements Listener {
	private final List<String> worlds;

	public CropTrampleProtectionListener(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onBlockChangeByEntity(EntityChangeBlockEvent e) {
		Block b = e.getBlock();
		// 判断是否为耕地
		if (b.getType() != Material.FARMLAND)
			return;
		// 检测是否为踩踏
		if (e.getTo() != Material.DIRT && e.getTo() != Material.GRASS_BLOCK)
			return;
                World world = getWorld(b);
                if (!isWorldEnabled(world))
                        return;
                // 取消事件
                e.setCancelled(true);
        }

        private World getWorld(Block block) {
                return block != null ? block.getWorld() : null;
        }

        private boolean isWorldEnabled(World world) {
                return world != null && worlds.contains(world.getName());
        }
}
