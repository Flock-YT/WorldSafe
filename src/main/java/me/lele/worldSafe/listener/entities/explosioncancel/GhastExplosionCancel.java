package me.lele.worldSafe.listener.entities.explosioncancel;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class GhastExplosionCancel implements Listener {
	private final List<String> worlds;

	public GhastExplosionCancel(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onFileballExplode(EntityExplodeEvent e) {
		// 检测实体是否为火球
		if (e.getEntityType() != EntityType.FIREBALL)
			return;
		Fireball ent = (Fireball) e.getEntity();
		// 检测此世界是否启用
		if (!worlds.contains(ent.getWorld().getName()))
			return;
		// 检测是否为恶魂发出的火球
		if (!(ent.getShooter() instanceof Ghast))
			return;
		// 清空受影响的方块
		e.setCancelled(true);
	}
}
