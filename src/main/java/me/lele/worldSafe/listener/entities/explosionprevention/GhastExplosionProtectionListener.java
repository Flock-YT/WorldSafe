package me.lele.worldSafe.listener.entities.explosionprevention;

import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class GhastExplosionProtectionListener extends AbstractWorldLimitedListener {

        public GhastExplosionProtectionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        void onFileballExplode(EntityExplodeEvent e) {
                // 检测实体是否为火球
                if (e.getEntityType() != EntityType.FIREBALL)
                        return;
                Fireball ent = (Fireball) e.getEntity();
                // 检测此世界是否启用
                if (!isWorldEnabled(ent))
                        return;
                // 检测是否为恶魂发出的火球
                if (!(ent.getShooter() instanceof Ghast))
                        return;
                // 清空受影响的方块
		e.blockList().clear();
	}
}
