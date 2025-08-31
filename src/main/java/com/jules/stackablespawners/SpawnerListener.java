package com.jules.stackablespawners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SpawnerListener implements Listener {

    private final StackableSpawners plugin;
    private final SpawnerManager spawnerManager;

    public SpawnerListener(StackableSpawners plugin) {
        this.plugin = plugin;
        this.spawnerManager = plugin.getSpawnerManager();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled() || event.getBlockPlaced().getType() != Material.SPAWNER) {
            return;
        }

        Block againstBlock = event.getBlockAgainst();
        ItemStack itemInHand = event.getItemInHand();

        if (againstBlock.getType() == Material.SPAWNER && itemInHand.getType() == Material.SPAWNER) {
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
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if(event.isCancelled()) return;

        Block block = event.getBlock();
        if (block.getType() != Material.SPAWNER) {
            return;
        }

        int stackSize = spawnerManager.getStackSize(block);
        if (stackSize > 1) {
            event.setExpToDrop(0);

            CreatureSpawner spawner = (CreatureSpawner) block.getState();
            ItemStack spawnerDrop = new ItemStack(Material.SPAWNER, stackSize);
            BlockStateMeta meta = (BlockStateMeta) spawnerDrop.getItemMeta();
            CreatureSpawner dropState = (CreatureSpawner) meta.getBlockState();
            dropState.setSpawnedType(spawner.getSpawnedType());
            meta.setBlockState(dropState);
            spawnerDrop.setItemMeta(meta);

            block.getWorld().dropItemNaturally(block.getLocation(), spawnerDrop);
        }
        spawnerManager.removeSpawner(block.getLocation());
    }
}
