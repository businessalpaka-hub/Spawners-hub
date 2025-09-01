package com.jules.stackablespawners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;

public class HopperListener implements Listener {

    private final StackableSpawners plugin;
    private final HopperManager hopperManager;
    private final ItemWorthManager itemWorthManager;
    private final Economy economy;

    public HopperListener(StackableSpawners plugin) {
        this.plugin = plugin;
        this.hopperManager = plugin.getHopperManager();
        this.itemWorthManager = plugin.getItemWorthManager();
        this.economy = plugin.getEconomy();
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Location destinationLoc = event.getDestination().getLocation();
        if (destinationLoc == null || !hopperManager.isMultiplierHopper(destinationLoc)) {
            return;
        }

        event.setCancelled(true);

        ItemStack item = event.getItem();
        double itemWorth = itemWorthManager.getItemWorth(item);
        HopperManager.HopperData hopperData = hopperManager.getHopperData(destinationLoc);
        OfflinePlayer owner = plugin.getServer().getOfflinePlayer(hopperData.getOwner());

        if (itemWorth > 0) {
            double totalWorth = itemWorth * item.getAmount() * hopperData.getMultiplier();

            if (owner.hasPlayedBefore() || owner.isOnline()) {
                economy.depositPlayer(owner, totalWorth);
            }

            // Remove the item from the source inventory
            event.getSource().removeItem(item);
        } else {
            // Item has no value, provide feedback to the owner if they are online.
            if (owner.isOnline()) {
                Player onlineOwner = (Player) owner;
                onlineOwner.sendMessage("§c[StackableSpawners] Your multiplier hopper at §e" +
                        destinationLoc.getBlockX() + ", " + destinationLoc.getBlockY() + ", " + destinationLoc.getBlockZ() +
                        "§c tried to sell §e" + item.getType().toString() + "§c, but it has no price in itemworth.yml.");
            }
        }
    }
}
