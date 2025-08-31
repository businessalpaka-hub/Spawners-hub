package com.jules.stackablespawners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class SpawnerTask extends BukkitRunnable {

    private final StackableSpawners plugin;
    private final SpawnerManager spawnerManager;
    private final LootManager lootManager;

    public SpawnerTask(StackableSpawners plugin) {
        this.plugin = plugin;
        this.spawnerManager = plugin.getSpawnerManager();
        this.lootManager = plugin.getLootManager();
    }

    @Override
    public void run() {
        for (Location loc : spawnerManager.getSpawnerLocations()) {
            if (!loc.getChunk().isLoaded()) {
                continue;
            }

            Block spawnerBlock = loc.getBlock();
            if (spawnerBlock.getType() != Material.SPAWNER) {
                continue;
            }

            Block belowBlock = loc.clone().subtract(0, 1, 0).getBlock();
            if (belowBlock.getType() != Material.HOPPER) {
                continue;
            }

            CreatureSpawner spawner = (CreatureSpawner) spawnerBlock.getState();
            Hopper hopper = (Hopper) belowBlock.getState();
            Inventory hopperInventory = hopper.getInventory();

            int stackSize = spawnerManager.getStackSize(spawnerBlock);
            Map<Material, Integer> drops = lootManager.getLoot(spawner.getSpawnedType());

            if (drops == null || drops.isEmpty()) {
                continue;
            }

            for (Map.Entry<Material, Integer> entry : drops.entrySet()) {
                Material material = entry.getKey();
                int amount = entry.getValue() * stackSize;

                // Make sure we don't overfill the hopper
                if (hopperInventory.firstEmpty() != -1) {
                     hopperInventory.addItem(new ItemStack(material, amount));
                }
            }
        }
    }
}
