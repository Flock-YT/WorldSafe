package me.lele.worldSafe.listener;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class TNTBlockDestructionProtection implements Listener {
	private final List<String> worlds;

	public TNTBlockDestructionProtection(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onTNTExplode(EntityExplodeEvent e) {
		if (e.getEntityType() != EntityType.TNT && e.getEntityType() != EntityType.TNT_MINECART)
			return;
		Entity ent = e.getEntity();
		if (ent.getWorld() == null)
			return;
		if (!worlds.contains(ent.getWorld().getName()))
			return;
		// 清空爆炸影响的方块
		e.blockList().clear();
	}
}
