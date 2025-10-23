package me.lele.worldSafe.listener.entities.explosioncancel;

import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class WitherExplosionCancelListener implements Listener {
	private final List<String> worlds;

	public WitherExplosionCancelListener(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onExplode(EntityExplodeEvent e) {
		// 检测是否为凋零/凋零头颅
		if (e.getEntityType() != EntityType.WITHER && e.getEntityType() != EntityType.WITHER_SKULL)
			return;
                Entity ent = e.getEntity();
                World world = getWorld(ent);
                if (!isWorldEnabled(world))
                        return;
                // 取消事件 阻止爆炸
                e.setCancelled(true);
        }

        private World getWorld(Entity entity) {
                return entity != null ? entity.getWorld() : null;
        }

        private boolean isWorldEnabled(World world) {
                return world != null && worlds.contains(world.getName());
        }
}
