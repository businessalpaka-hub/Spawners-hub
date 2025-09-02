package de.user.sellgui;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SellReloadCommand implements CommandExecutor {

    private final SellGUI plugin;

    public SellReloadCommand(SellGUI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sellgui.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }

        plugin.reloadConfiguration();
        sender.sendMessage("SellGUI configuration and prices have been reloaded.");

        return true;
    }
}
