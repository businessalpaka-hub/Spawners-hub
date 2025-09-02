package de.user.sellgui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class GUIManager {

    private final SellGUI plugin;
    private final PriceManager priceManager;

    public GUIManager(SellGUI plugin) {
        this.plugin = plugin;
        this.priceManager = plugin.getPriceManager();
    }

    public void openSellGUI(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "&2&lSell Menu"));
        Inventory sellGUI = Bukkit.createInventory(null, 54, title);

        // Add filler items and total worth item
        createGUIItems(sellGUI);

        player.openInventory(sellGUI);
    }

    public void createGUIItems(Inventory gui) {
        // Filler Item
        if (plugin.getConfig().getBoolean("filler-item.enabled", true)) {
            Material fillerMaterial = Material.matchMaterial(plugin.getConfig().getString("filler-item.material", "GRAY_STAINED_GLASS_PANE"));
            if (fillerMaterial == null) fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
            ItemStack fillerItem = new ItemStack(fillerMaterial);
            ItemMeta fillerMeta = fillerItem.getItemMeta();
            if (fillerMeta != null) {
                fillerMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("filler-item.name", " ")));
                fillerItem.setItemMeta(fillerMeta);
            }
            for (int i = 45; i < 53; i++) {
                gui.setItem(i, fillerItem);
            }
        }

        // Total Worth Item
        updateTotalWorth(gui);
    }

    public void updateTotalWorth(Inventory gui) {
        double total = calculateTotalWorth(gui);

        Material itemMaterial = Material.matchMaterial(plugin.getConfig().getString("total-worth-item.material", "GOLD_NUGGET"));
        if (itemMaterial == null) itemMaterial = Material.GOLD_NUGGET;
        ItemStack totalWorthItem = new ItemStack(itemMaterial);
        ItemMeta meta = totalWorthItem.getItemMeta();

        if (meta != null) {
            String name = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("total-worth-item.name", "&6&lTotal Worth"));
            meta.setDisplayName(name);

            List<String> lore = plugin.getConfig().getStringList("total-worth-item.lore");
            String formattedTotal = String.format("%.2f", total);
            List<String> coloredLore = lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("%total%", formattedTotal)))
                    .collect(Collectors.toList());
            meta.setLore(coloredLore);
            totalWorthItem.setItemMeta(meta);
        }

        gui.setItem(53, totalWorthItem);
    }

    public double calculateTotalWorth(Inventory gui) {
        double total = 0;
        for (int i = 0; i < 45; i++) { // Only check the top part of the GUI
            ItemStack item = gui.getItem(i);
            if (item != null) {
                total += priceManager.getItemPrice(item) * item.getAmount();
            }
        }
        return total;
    }
}
