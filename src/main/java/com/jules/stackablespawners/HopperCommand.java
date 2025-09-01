package com.jules.stackablespawners;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

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

        if (args.length < 2) {
            player.sendMessage("Usage: /hopper <multiplier> [name]");
            return true;
        }

        double multiplier;
        try {
            multiplier = Double.parseDouble(args[0]);
            if (multiplier <= 0) {
                player.sendMessage("Multiplier must be a positive number.");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid multiplier: " + args[0]);
            return true;
        }

        String name = null;
        if (args.length >= 2) {
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                nameBuilder.append(args[i]).append(" ");
            }
            name = nameBuilder.toString().trim();
        }

        giveMultiplierHopper(player, multiplier, name);
        player.sendMessage("You have received a multiplier hopper!");

        return true;
    }

    private void giveMultiplierHopper(Player player, double multiplier, String name) {
        ItemStack hopperItem = new ItemStack(Material.HOPPER);
        ItemMeta meta = hopperItem.getItemMeta();

        meta.getPersistentDataContainer().set(hopperManager.ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());
        meta.getPersistentDataContainer().set(hopperManager.multiplierKey, PersistentDataType.DOUBLE, multiplier);
        if (name != null) {
            meta.setDisplayName(name);
            meta.getPersistentDataContainer().set(hopperManager.nameKey, PersistentDataType.STRING, name);
        }

        List<String> lore = new ArrayList<>();
        lore.add("§dMultiplier Hopper");
        lore.add("§7Multiplier: §a" + multiplier);
        if (name != null) {
            lore.add("§7Name: §a" + name);
        }
        lore.add("§7Owner: §a" + player.getName());
        meta.setLore(lore);

        hopperItem.setItemMeta(meta);
        player.getInventory().addItem(hopperItem);
    }
}
