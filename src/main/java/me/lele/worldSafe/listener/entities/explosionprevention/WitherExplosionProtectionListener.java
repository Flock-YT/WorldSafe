package me.lele.worldSafe.listener.entities.explosionprevention;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class WitherExplosionProtectionListener implements Listener {
	private final List<String> worlds;

	public WitherExplosionProtectionListener(List<String> worlds) {
		this.worlds = worlds;
	}

	@EventHandler
	void onWitherDestroyBlock(EntityChangeBlockEvent e) {
		// 判断是否为凋零/凋零头
		if (e.getEntityType() != EntityType.WITHER && e.getEntityType() != EntityType.WITHER_SKULL)
			return;
                Entity ent = e.getEntity();
                World entityWorld = getWorld(ent);
                if (!isWorldEnabled(entityWorld))
                        return;
                World blockWorld = getWorld(e.getBlock());
                if (!isSameWorld(entityWorld, blockWorld))
                        return;
                // 判断是否破坏方块
                if (e.getTo() != Material.AIR)
                        return;
                // 取消事件
                e.setCancelled(true);
        }

        private World getWorld(Entity entity) {
                return entity != null ? entity.getWorld() : null;
        }

        private World getWorld(Block block) {
                return block != null ? block.getWorld() : null;
        }

        private boolean isSameWorld(World first, World second) {
                return first != null && first.equals(second);
        }

        private boolean isWorldEnabled(World world) {
                return world != null && worlds.contains(world.getName());
        }
}
