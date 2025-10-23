package me.lele.worldSafe.listener.entities.explosionprevention;

import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class GhastExplosionProtectionListener extends WorldScopedFeature {

        public GhastExplosionProtectionListener(List<String> worlds) {
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
                e.blockList().clear();
        }
}
