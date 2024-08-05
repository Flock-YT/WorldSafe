package me.lele.worldSafe.listener.blocks.other;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class DragonEggTeleportationPrevention implements Listener {
	private final List<String> worlds;

	public DragonEggTeleportationPrevention(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onDragonEggTeleport(BlockFromToEvent e) {
		Block b = e.getBlock();
		// 检测方块是否为龙蛋
		if (b.getType() != Material.DRAGON_EGG)
			return;
        // 检测世界是否为null值
		if (b.getWorld() == null)
			return;
		// 判断是否启动这个世界
		if (!worlds.contains(b.getWorld().getName()))
			return;
		// 取消事件 即禁止龙蛋瞬移
		e.setCancelled(true);
	}
}
