package de.user.sellgui;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;

public class SellGUIListener implements Listener {

    private final SellGUI plugin;
    private final GUIManager guiManager;
    private final PriceManager priceManager;
    private final DecimalFormat numberFormat;

    public SellGUIListener(SellGUI plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        this.priceManager = plugin.getPriceManager();
        String format = plugin.getConfig().getString("gui.number-format", "#,##0.00");
        this.numberFormat = new DecimalFormat(format);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "&2&lSell Menu"));
        if (!event.getView().getTitle().equals(title)) {
            return;
        }

        if (event.getSlot() >= 45) {
            event.setCancelled(true);
        }

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
        boolean debug = plugin.getConfig().getBoolean("gui.debug-unpriced-items", true);

        for (int i = 0; i < 45; i++) {
            ItemStack item = gui.getItem(i);
            if (item != null && item.getType() != Material.AIR) {
                double price = priceManager.getItemPrice(item);
                if (price > 0) {
                    totalSold += price * item.getAmount();
                } else if (debug) {
                    String itemKey = item.getType().toString().toLowerCase().replace("_", "");
                    player.sendMessage(ChatColor.RED + "[SellGUI Debug] Item '" + itemKey + "' has no price in prices.yml.");
                }
            }
        }

        if (totalSold > 0) {
            plugin.getEconomy().depositPlayer(player, totalSold);
            String sellMessageFormat = plugin.getConfig().getString("sell-message", "&a+%price% €");
            String formattedTotal = numberFormat.format(totalSold);
            String message = ChatColor.translateAlternateColorCodes('&', sellMessageFormat.replace("%price%", formattedTotal));

            player.sendActionBar(Component.text(message));
        }
    }
}
