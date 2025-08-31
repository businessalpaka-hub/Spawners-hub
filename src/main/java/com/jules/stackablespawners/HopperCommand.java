package com.jules.stackablespawners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HopperCommand implements CommandExecutor {

    private final StackableSpawners plugin;
    private final HopperManager hopperManager;

    public HopperCommand(StackableSpawners plugin) {
        this.plugin = plugin;
        this.hopperManager = plugin.getHopperManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || !args[0].equalsIgnoreCase("create")) {
            player.sendMessage("Usage: /hopper create <multiplier> [name]");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage("Usage: /hopper create <multiplier> [name]");
            return true;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[1]);
            if (multiplier <= 0) {
                player.sendMessage("Multiplier must be a positive number.");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid multiplier: " + args[1]);
            return true;
        }

        String name = null;
        if (args.length >= 3) {
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                nameBuilder.append(args[i]).append(" ");
            }
            name = nameBuilder.toString().trim();
        }

        Block targetBlock = player.getTargetBlock(null, 5);
        if (targetBlock.getType() != Material.HOPPER) {
            player.sendMessage("You must be looking at a hopper.");
            return true;
        }

        if (hopperManager.isMultiplierHopper(targetBlock.getLocation())) {
            player.sendMessage("This hopper is already a multiplier hopper.");
            return true;
        }

        hopperManager.addHopper(targetBlock.getLocation(), player.getUniqueId(), multiplier, name);
        player.sendMessage("Successfully created a multiplier hopper with a multiplier of " + multiplier + "!");

        return true;
    }
}
