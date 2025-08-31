package com.jules.stackablespawners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemWorthManager {

    private final StackableSpawners plugin;
    private final Map<String, Double> itemWorth = new HashMap<>();

    public ItemWorthManager(StackableSpawners plugin) {
        this.plugin = plugin;
    }

    public void loadItemWorth() {
        itemWorth.clear();
        FileConfiguration config = plugin.getItemWorthConfig();
        if (config.getConfigurationSection("worth") == null) {
            return;
        }
        for (String itemName : config.getConfigurationSection("worth").getKeys(false)) {
            double worth = config.getDouble("worth." + itemName);
            itemWorth.put(itemName, worth);
        }
    }

    public double getItemWorth(ItemStack item) {
        if (item == null) {
            return 0.0;
        }
        // Key format: lowercase, no spaces or underscores
        String key = item.getType().toString().toLowerCase().replace("_", "");
        return itemWorth.getOrDefault(key, 0.0);
    }
}
