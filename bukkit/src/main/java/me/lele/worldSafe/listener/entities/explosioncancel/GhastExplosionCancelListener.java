package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.List;

public class GhastExplosionCancelListener extends WorldScopedFeature {

        public GhastExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFireballExplode(EntityExplodeEvent e) {
                if (!(e.getEntity() instanceof Fireball))
                        return;
                Fireball ent = (Fireball) e.getEntity();
                if (!isWorldEnabled(getWorld(ent)))
                        return;
                // 检测是否为恶魂发出的火球
                if (!(ent.getShooter() instanceof Ghast))
                        return;
                // 清空受影响的方块
                e.setCancelled(true);
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onFireballPrime(ExplosionPrimeEvent event) {
                if (isGhastFireball(event.getEntity()) && isWorldEnabled(getWorld(event.getEntity()))) {
                        event.setCancelled(true);
                }
        }

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
                if (!(e.getDamager() instanceof Fireball))
                        return;
                Fireball ent = (Fireball) e.getDamager();
                if (!isWorldEnabled(getWorld(ent)))
                        return;
                // 检测是否为恶魂发出的火球
                if (!(ent.getShooter() instanceof Ghast))
                        return;
                //消除火球伤害
                e.setCancelled(true);
        }

        private boolean isGhastFireball(Entity entity) {
                return entity instanceof Fireball && ((Fireball) entity).getShooter() instanceof Ghast;
        }

}
