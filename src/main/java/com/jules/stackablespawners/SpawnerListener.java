package com.jules.stackablespawners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class SpawnerListener implements Listener {

    private final StackableSpawners plugin;
    private final SpawnerManager spawnerManager;
    private final NamespacedKey stackSizeKey;

    public SpawnerListener(StackableSpawners plugin) {
        this.plugin = plugin;
        this.spawnerManager = plugin.getSpawnerManager();
        this.stackSizeKey = new NamespacedKey(plugin, "spawner_stack_size");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        if (placedBlock.getType() != Material.SPAWNER) {
            return;
        }

        ItemStack itemInHand = event.getItemInHand();
        ItemMeta meta = itemInHand.getItemMeta();

        // Check if the item has a pre-defined stack size
        if (meta != null && meta.getPersistentDataContainer().has(stackSizeKey, PersistentDataType.INTEGER)) {
            int level = meta.getPersistentDataContainer().get(stackSizeKey, PersistentDataType.INTEGER);
            spawnerManager.setStackSize(placedBlock, level);
            if (level > 1) {
                spawnerManager.addSpawner(placedBlock.getLocation());
            }
            event.getPlayer().sendMessage("Placed a level " + level + " spawner.");
            return;
        }

        // --- Logic for stacking by placing on another spawner ---
        Block againstBlock = event.getBlockAgainst();
        if (againstBlock.getType() == Material.SPAWNER) {
            BlockStateMeta itemMeta = (BlockStateMeta) itemInHand.getItemMeta();
            CreatureSpawner itemSpawnerState = (CreatureSpawner) itemMeta.getBlockState();
            CreatureSpawner againstSpawner = (CreatureSpawner) againstBlock.getState();

            if (itemSpawnerState.getSpawnedType() == againstSpawner.getSpawnedType()) {
                event.setCancelled(true);

                int currentSize = spawnerManager.getStackSize(againstBlock);
                int maxStackSize = plugin.getConfig().getInt("spawners.max-stack-size", 20);

                if (currentSize < maxStackSize) {
                    spawnerManager.setStackSize(againstBlock, currentSize + 1);
                    spawnerManager.addSpawner(againstBlock.getLocation());

                    if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                    }
                    event.getPlayer().sendMessage("Spawner stack size increased to " + (currentSize + 1));
                } else {
                    event.getPlayer().sendMessage("This spawner is already at its maximum stack size of " + maxStackSize);
                }
            }
        } else {
            // It's a new spawner being placed, not on another spawner, and with no level data.
            // Initialize it with stack size 1.
            spawnerManager.setStackSize(placedBlock, 1);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        int stackSize = spawnerManager.getStackSize(block);
        spawnerManager.removeSpawner(block.getLocation());

        if (stackSize > 1) {
            event.setCancelled(true); // Prevent default drop
            event.setExpToDrop(0);

            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            ItemStack spawnerDrop = new ItemStack(Material.SPAWNER, 1); // Drop a single item

            BlockStateMeta meta = (BlockStateMeta) spawnerDrop.getItemMeta();
            // Set spawner type
            CreatureSpawner dropState = (CreatureSpawner) meta.getBlockState();
            dropState.setSpawnedType(spawner.getSpawnedType());
            meta.setBlockState(dropState);

            // Set stack level on the dropped item
            meta.getPersistentDataContainer().set(stackSizeKey, PersistentDataType.INTEGER, stackSize);

            spawnerDrop.setItemMeta(meta);

            block.getWorld().dropItemNaturally(block.getLocation(), spawnerDrop);
        }
    }
}
