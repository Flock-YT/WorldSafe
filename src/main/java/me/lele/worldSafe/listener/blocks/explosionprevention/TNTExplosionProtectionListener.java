package me.lele.worldSafe.listener.blocks.explosionprevention;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import me.lele.worldSafe.listener.AbstractWorldLimitedListener;

public class TNTExplosionProtectionListener extends AbstractWorldLimitedListener {

        public TNTExplosionProtectionListener(List<String> worlds) {
                super(worlds);
        }

        @EventHandler
        void onTNTExplode(EntityExplodeEvent e) {
                // 检测是否为TNT类实体
                if (e.getEntityType() != EntityType.TNT && e.getEntityType() != EntityType.TNT_MINECART)
                        return;
                Entity ent = e.getEntity();
                // 判断是否启用这个世界
                if (!isWorldEnabled(ent))
                        return;
                // 清空爆炸影响的方块
                e.blockList().clear();
        }
}
