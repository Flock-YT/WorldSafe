package me.lele.worldSafe.listener.biological.explosionprevention;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class CreeperExplosionProtectionListener implements Listener {

    //定义生效的世界
    private final List<String> worlds;

    //把配置文件中的生效世界赋值到worlds
    public CreeperExplosionProtectionListener(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        // 检查是否是Creeper的爆炸
        if (event.getEntityType() == EntityType.CREEPER) {
            //获取事件触发的世界
            World world = event.getLocation().getWorld();
            //判断是否属于生效的世界
            if (world != null && worlds.contains(world.getName())) {
                //阻止事件
                event.blockList().clear();
            }
        }
    }

}
