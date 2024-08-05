package me.lele.worldSafe.listener.entities.explosioncancel;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WitherExplosionCancel implements Listener {
	private final List<String> worlds;

	public WitherExplosionCancel(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onExplode(EntityExplodeEvent e) {
		// 检测是否为凋零/凋零头颅
		if (e.getEntityType() != EntityType.WITHER && e.getEntityType() != EntityType.WITHER_SKULL)
			return;
		Entity ent = e.getEntity();
		// 检测是否启用这个世界
		if (!worlds.contains(ent.getWorld().getName()))
			return;
		// 取消事件 阻止爆炸
		e.setCancelled(true);
	}
}
