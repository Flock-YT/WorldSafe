package me.lele.worldSafe.listener.entities.explosioncancel;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class GhastExplosionCancelListener extends WorldScopedFeature {

        public GhastExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	void onFileballExplode(EntityExplodeEvent e) {
		// 检测实体是否为火球
		if (e.getEntityType() != EntityType.FIREBALL)
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

	@EventHandler
	void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		// 检测造成伤害的实体是否为火球
		if (e.getDamager().getType() != EntityType.FIREBALL)
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

}
