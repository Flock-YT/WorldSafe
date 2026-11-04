package me.lele.worldSafe.command;

import me.lele.worldSafe.WorldSafe;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class WorldSafeCommand implements CommandExecutor, TabCompleter {

    private final WorldSafe plugin;

    public WorldSafeCommand(WorldSafe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("worldsafe.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (plugin.reloadWorldSafe()) {
                sender.sendMessage("WorldSafe configuration reloaded.");
            } else {
                sender.sendMessage("WorldSafe reload failed. The previous configuration is still active.");
            }
            return true;
        }

        sender.sendMessage("/worldsafe reload - Reload the configuration");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !sender.hasPermission("worldsafe.admin")) {
            return Collections.emptyList();
        }
        String prefix = args[0].toLowerCase(Locale.ROOT);
        if ("reload".startsWith(prefix)) {
            return Arrays.asList("reload");
        }
        if ("help".startsWith(prefix)) {
            return Arrays.asList("help");
        }
        return Collections.emptyList();
    }
}
