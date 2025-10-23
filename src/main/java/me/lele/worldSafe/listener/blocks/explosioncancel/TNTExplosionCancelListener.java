package me.lele.worldSafe.listener.blocks.explosioncancel;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class TNTExplosionCancelListener implements Listener {
	private final List<String> worlds;

	public TNTExplosionCancelListener(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onTNTExplode(EntityExplodeEvent e) {
		// 检测是否为TNT类实体
		if (e.getEntityType() != EntityType.TNT && e.getEntityType() != EntityType.TNT_MINECART)
			return;
                Entity ent = e.getEntity();
                World world = getWorld(ent);
                if (!isWorldEnabled(world))
                        return;
                // 清空爆炸影响的方块
                e.setCancelled(true);
        }

        private World getWorld(Entity entity) {
                return entity != null ? entity.getWorld() : null;
        }

        private boolean isWorldEnabled(World world) {
                return world != null && worlds.contains(world.getName());
        }
}
