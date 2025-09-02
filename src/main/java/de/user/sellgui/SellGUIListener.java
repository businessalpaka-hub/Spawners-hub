package de.user.sellgui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SellGUIListener implements Listener {

    private final SellGUI plugin;
    private final GUIManager guiManager;
    private final PriceManager priceManager;

    public SellGUIListener(SellGUI plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        this.priceManager = plugin.getPriceManager();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "&2&lSell Menu"));
        if (!event.getView().getTitle().equals(title)) {
            return;
        }

        // Prevent taking the placeholder items
        if (event.getSlot() >= 45) {
            event.setCancelled(true);
        }

        // Update the total worth on the next tick
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            guiManager.updateTotalWorth(event.getInventory());
        }, 1L);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "&2&lSell Menu"));
        if (!event.getView().getTitle().equals(title)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        Inventory gui = event.getInventory();
        double totalSold = 0;

        for (int i = 0; i < 45; i++) {
            ItemStack item = gui.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                totalSold += priceManager.getItemPrice(item) * item.getAmount();
            }
        }

        if (totalSold > 0) {
            plugin.getEconomy().depositPlayer(player, totalSold);
            String sellMessageFormat = plugin.getConfig().getString("sell-message", "&a+%price% €");
            String formattedTotal = String.format("%.2f", totalSold);
            String message = ChatColor.translateAlternateColorCodes('&', sellMessageFormat.replace("%price%", formattedTotal));

            player.sendActionBar(Component.text(message));
        }
    }
}
