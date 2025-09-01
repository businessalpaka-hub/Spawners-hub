package com.jules.stackablespawners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class HopperPlaceListener implements Listener {

    private final HopperManager hopperManager;

    public HopperPlaceListener(HopperManager hopperManager) {
        this.hopperManager = hopperManager;
    }

    @EventHandler
    public void onHopperPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.HOPPER) {
            return;
        }

        ItemStack item = event.getItemInHand();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(hopperManager.ownerKey, PersistentDataType.STRING) &&
            container.has(hopperManager.multiplierKey, PersistentDataType.DOUBLE)) {

            UUID owner = UUID.fromString(container.get(hopperManager.ownerKey, PersistentDataType.STRING));
            double multiplier = container.get(hopperManager.multiplierKey, PersistentDataType.DOUBLE);
            String name = container.get(hopperManager.nameKey, PersistentDataType.STRING);

            hopperManager.addHopper(block.getLocation(), owner, multiplier, name);
            event.getPlayer().sendMessage("Multiplier hopper successfully placed!");
        }
    }
}
