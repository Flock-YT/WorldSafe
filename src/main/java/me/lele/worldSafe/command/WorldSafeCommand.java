package me.lele.worldSafe.command;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.lele.worldSafe.WorldSafe;
import org.bukkit.command.CommandSender;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.logging.Level;

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
        sender.sendMessage("/worldsafe reload - 重载命令");
    }

    @Execute(name = "reload")
    void reloadCommand(@Context CommandSender sender) {
        try {
            plugin.reloadFeatures();
            sender.sendMessage("配置已重载！");
        } catch (ConfigurateException | SerializationException e) {
            plugin.getLogger().log(Level.SEVERE, "重载配置失败", e);
            sender.sendMessage("配置重载失败，请查看控制台以获取详细信息。");
        }
    }

}
