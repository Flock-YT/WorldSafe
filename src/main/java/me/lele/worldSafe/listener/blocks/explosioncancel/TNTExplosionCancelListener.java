package me.lele.worldSafe.listener.blocks.explosioncancel;

import me.lele.worldSafe.compat.EntityTypeMatcher;
import me.lele.worldSafe.listener.WorldScopedFeature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class TNTExplosionCancelListener extends WorldScopedFeature {

        public TNTExplosionCancelListener(List<String> worlds) {
                super(worlds);
        }

	@EventHandler
	void onTNTExplode(EntityExplodeEvent e) {
                if (!EntityTypeMatcher.matches(e.getEntity(), "PRIMED_TNT", "TNT", "MINECART_TNT", "TNT_MINECART"))
                        return;
                if (!isWorldEnabled(getWorld(e.getEntity())))
                        return;
                // 清空爆炸影响的方块
                e.setCancelled(true);
        }
}
