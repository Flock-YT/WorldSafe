package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.List;

public class WitherExplosionCancelListener extends WorldScopedFeature {

        public WitherExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExplode(EntityExplodeEvent e) {
		// 检测是否为凋零/凋零头颅
		if (!isWitherExplosion(e.getEntity()))
			return;
                if (!isWorldEnabled(getWorld(e.getEntity())))
                        return;
                // 取消事件 阻止爆炸
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onExplosionPrime(ExplosionPrimeEvent event) {
                if (isWitherExplosion(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
                        event.setCancelled(true);
                }
        }

        private boolean isWitherExplosion(Entity entity) {
                return EntityTypeMatcher.matches(entity, "WITHER", "WITHER_SKULL");
        }
}
