package me.lele.worldSafe.listener.blocks.explosioncancel;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.List;

public class TNTExplosionCancelListener extends WorldScopedFeature {

        public TNTExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTNTExplode(EntityExplodeEvent e) {
                if (!isTnt(e.getEntity()))
                        return;
                if (!isWorldEnabled(getWorld(e.getEntity())))
                        return;
                // 清空爆炸影响的方块
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onTNTPrime(ExplosionPrimeEvent event) {
                if (isTnt(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
                        event.setCancelled(true);
                }
        }

        private boolean isTnt(Entity entity) {
                return EntityTypeMatcher.matches(entity, "PRIMED_TNT", "TNT", "MINECART_TNT", "TNT_MINECART");
        }
}
