package com.jules.stackablespawners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerCommand implements CommandExecutor {

    private final StackableSpawners plugin;

    public SpawnerCommand(StackableSpawners plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /sspawner <reload|give>");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getLootManager().loadLootTables();
            plugin.getItemWorthManager().loadItemWorth();
            sender.sendMessage("StackableSpawners configuration reloaded.");
            return true;
        }

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /sspawner give <type> <level> [player]");
                return true;
            }

            EntityType type;
            try {
                type = EntityType.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Invalid entity type: " + args[1]);
                return true;
            }

            int level;
            try {
                level = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid level: " + args[2]);
                return true;
            }

            int maxStackSize = plugin.getConfig().getInt("spawners.max-stack-size", 20);
            if (level > maxStackSize) {
                level = maxStackSize;
                sender.sendMessage("Level was capped at the max stack size of " + maxStackSize);
            }
            if (level <= 0) {
                sender.sendMessage("Level must be a positive number.");
                return true;
            }

            Player target = null;
            if (args.length >= 4) {
                target = plugin.getServer().getPlayer(args[3]);
                if (target == null) {
                    sender.sendMessage("Player not found: " + args[3]);
                    return true;
                }
            } else if (sender instanceof Player) {
                target = (Player) sender;
            } else {
                sender.sendMessage("You must specify a player from the console.");
                return true;
            }

            giveSpawner(target, type, level);
            sender.sendMessage("Gave a level " + level + " " + type.name() + " spawner to " + target.getName());
            return true;
        }

        return false;
    }

    private void giveSpawner(Player player, EntityType type, int level) {
        ItemStack spawnerItem = new ItemStack(Material.SPAWNER, 1);
        BlockStateMeta meta = (BlockStateMeta) spawnerItem.getItemMeta();

        // Set spawner type
        CreatureSpawner spawnerState = (CreatureSpawner) meta.getBlockState();
        spawnerState.setSpawnedType(type);
        meta.setBlockState(spawnerState);

        // Set stack level on the item's PDC
        NamespacedKey stackSizeKey = new NamespacedKey(plugin, "spawner_stack_size");
        meta.getPersistentDataContainer().set(stackSizeKey, PersistentDataType.INTEGER, level);

        spawnerItem.setItemMeta(meta);
        player.getInventory().addItem(spawnerItem);
    }
}
