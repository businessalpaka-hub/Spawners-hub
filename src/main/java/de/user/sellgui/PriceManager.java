package de.user.sellgui;

import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.util.HashMap;
import java.util.Map;

public class PriceManager {

    private final SellGUI plugin;
    private final Map<String, Double> prices = new HashMap<>();

    public PriceManager(SellGUI plugin) {
        this.plugin = plugin;
    }

    public void loadPrices() {
        prices.clear();
        ConfigurationSection pricesSection = plugin.getPricesConfig().getConfigurationSection("prices");
        if (pricesSection == null) {
            plugin.getLogger().warning("No 'prices' section found in prices.yml!");
            return;
        }
        for (String key : pricesSection.getKeys(false)) {
            prices.put(key.toLowerCase(), pricesSection.getDouble(key));
        }
    }

    public double getItemPrice(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return 0.0;
        }

        // Handle Shulker Boxes
        if (item.getItemMeta() instanceof BlockStateMeta) {
            BlockStateMeta bsm = (BlockStateMeta) item.getItemMeta();
            if (bsm.getBlockState() instanceof ShulkerBox) {
                ShulkerBox shulker = (ShulkerBox) bsm.getBlockState();
                double shulkerValue = 0;
                for (ItemStack shulkerItem : shulker.getInventory().getContents()) {
                     if (shulkerItem != null && !(shulkerItem.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta)shulkerItem.getItemMeta()).getBlockState() instanceof ShulkerBox) ) {
                        shulkerValue += getItemPrice(shulkerItem) * shulkerItem.getAmount();
                    }
                }
                String key = item.getType().toString().toLowerCase().replace("_", "");
                return prices.getOrDefault(key, 0.0) + shulkerValue;
            }
        }

        // Handle normal items
        String key = item.getType().toString().toLowerCase().replace("_", "");
        return prices.getOrDefault(key, 0.0);
    }
}
