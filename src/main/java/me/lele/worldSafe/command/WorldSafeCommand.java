package me.lele.worldSafe.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.lele.worldSafe.WorldSafe;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import static me.lele.worldSafe.WorldSafe.configManager;
import static me.lele.worldSafe.WorldSafe.listeners;

@Command(name = "worldsafe")
@Permission("worldsafe.admin")
public class WorldSafeCommand {

    private final WorldSafe plugin;

    public WorldSafeCommand(WorldSafe plugin) {
        this.plugin = plugin;
    }

    @Execute
    public void onMainCommand(@Context CommandSender sender) {
        sender.sendMessage("/worldsafe reload - 重载命令");
    }

    @Execute(name = "help")
    public void helpCommand(@Context CommandSender sender) {
        sender.sendMessage("/worldSafe reload - 重载命令");
    }

    @Execute(name = "reload")
    void reloadCommand(@Context CommandSender sender) {
        //重载配置
        configManager.reloadConfig();
        //套上线程同步锁,保证重载期间服务器停止处理,避免有生物在重载的几毫秒时间里破坏方块
        synchronized (this) {
            //重载监听器
            for (Listener listener : listeners) {
                HandlerList.unregisterAll(listener);
            }
            // 清空监听器列表
            listeners.clear();
            //重新注册监听器
            plugin.reloadFeatures();
        }

        sender.sendMessage("配置已重载！");
    }

}
