package me.lele.worldSafe.listener.blocks.explosioncancel;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class BedExplosionCancelListener implements Listener {

	private final List<String> worlds;

	public BedExplosionCancelListener(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onPlayerInteractEvent(PlayerInteractEvent e) {
		// 检查玩家是否为右键点击
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;
		// 检查玩家点击的是否为方块
		if (!e.hasBlock())
			return;
		World w = e.getClickedBlock().getWorld();
		// 判断是否在主世界触发
		if (w.getEnvironment() == Environment.NORMAL)
			return;
		// 检查玩家点击的是否为床
		if (!(e.getClickedBlock().getBlockData() instanceof Bed))
			return;
		// 判断是否启用这个世界
		if (!worlds.contains(w.getName()))
			return;
		// 取消事件，防止爆炸
		e.setCancelled(true);

	}

}
