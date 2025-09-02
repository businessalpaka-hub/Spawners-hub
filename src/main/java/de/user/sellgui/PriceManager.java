package de.user.sellgui;

import org.bukkit.Keyed;
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
            // Keys are now case-sensitive and include namespace, e.g., "minecraft:stone"
            prices.put(key, pricesSection.getDouble(key));
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
                    // Recursive call to prevent selling nested shulkers inside shulkers
                    if (shulkerItem != null && !(shulkerItem.getItemMeta() instanceof BlockStateMeta && ((BlockStateMeta)shulkerItem.getItemMeta()).getBlockState() instanceof ShulkerBox) ) {
                        shulkerValue += getItemPrice(shulkerItem) * shulkerItem.getAmount();
                    }
                }
                // Add the price of the box itself
                String key = ((Keyed) item.getType()).getKey().toString();
                return prices.getOrDefault(key, 0.0) + shulkerValue;
            }
        }

        // Handle normal items
        String key = ((Keyed) item.getType()).getKey().toString();
        return prices.getOrDefault(key, 0.0);
    }
}
