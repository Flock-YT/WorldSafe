package me.lele.worldSafe.listener.blocks.other;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class CropTrampleProtection implements Listener {
	private final List<String> worlds;

	public CropTrampleProtection(List<String> worlds) {
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
		// 检测世界是否启用
		if (!worlds.contains(b.getLocation().getWorld().getName()))
			return;
		// 取消事件
		e.setCancelled(true);
	}
}
