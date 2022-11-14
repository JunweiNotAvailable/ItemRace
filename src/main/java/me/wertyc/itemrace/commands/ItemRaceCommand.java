package me.wertyc.itemrace.commands;

import me.wertyc.itemrace.ItemRace;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ItemRaceCommand implements CommandExecutor {
    private final ItemRace plugin;

    public ItemRaceCommand(ItemRace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("itemrace")) {
            if (args.length == 0) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("A player is required to run this command");
                    return true;
                }
                plugin.openMenu((Player) sender);
            } else {
                if (args[0].equals("stop")) {
                    plugin.stop();
                }
            }
        }
        return true;
    }
}
