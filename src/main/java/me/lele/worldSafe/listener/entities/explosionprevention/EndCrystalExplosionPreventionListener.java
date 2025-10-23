package me.lele.worldSafe.listener.entities.explosionprevention;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.Collection;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class EndCrystalExplosionPreventionListener extends AbstractWorldLimitedListener {

    public EndCrystalExplosionPreventionListener(Collection<String> worlds) {
        super(worlds);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // 检查爆炸实体是否是末地水晶
        if (event.getEntity().getType() == EntityType.END_CRYSTAL) {
            // 判断是否启用这个世界
            if (!isWorldEnabled(event.getLocation()))
                return;
            // 清空爆炸影响的方块
            event.blockList().clear();
        }
    }

}
