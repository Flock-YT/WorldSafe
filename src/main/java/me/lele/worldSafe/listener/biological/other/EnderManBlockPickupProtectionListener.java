package me.lele.worldSafe.listener.biological.other;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.List;

public class EnderManBlockPickupProtectionListener implements Listener {

    //定义生效的世界
    private final List<String> worlds;

    //把配置文件中的生效世界赋值到worlds
    public EnderManBlockPickupProtectionListener(List<String> worlds) {
        this.worlds = worlds;
    }

    @EventHandler
    public void onEnderManBlockPickup(EntityChangeBlockEvent event) {
        // 检查是否是末影人
        if (event.getEntityType() == EntityType.ENDERMAN) {
            //获取事件触发的世界
            World world = event.getBlock().getWorld();

            //判断是否属于生效的世界
            if (world != null && worlds.contains(world.getName())) {
                //阻止事件
                event.setCancelled(true);
            }

        }

    }

}
