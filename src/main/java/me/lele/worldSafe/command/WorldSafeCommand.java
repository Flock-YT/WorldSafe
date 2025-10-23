package me.lele.worldSafe.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.lele.worldSafe.WorldSafe;
import org.bukkit.command.CommandSender;

import static me.lele.worldSafe.WorldSafe.configManager;

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
        if (!configManager.reloadConfig()) {
            plugin.getLogger().severe("重载配置失败，已保留原有配置。");
            sender.sendMessage("配置重载失败，请检查控制台日志。");
            return;
        }
        plugin.resetFeatures();

        sender.sendMessage("配置已重载！");
    }

}
