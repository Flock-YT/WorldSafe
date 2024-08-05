package me.lele.worldSafe.listener.blocks.explosionprevention;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TNTExplosionProtection implements Listener {
	private final List<String> worlds;

	public TNTExplosionProtection(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onTNTExplode(EntityExplodeEvent e) {
		// 检测是否为TNT类实体
		if (e.getEntityType() != EntityType.TNT && e.getEntityType() != EntityType.TNT_MINECART)
			return;
		Entity ent = e.getEntity();
		// 判断是否启用这个世界
		if (!worlds.contains(ent.getWorld().getName()))
			return;
		// 清空爆炸影响的方块
		e.blockList().clear();
	}
}
