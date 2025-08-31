package com.jules.stackablespawners;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class LootManager {

    private final StackableSpawners plugin;
    private final Map<EntityType, Map<Material, Integer>> lootTables = new EnumMap<>(EntityType.class);

    public LootManager(StackableSpawners plugin) {
        this.plugin = plugin;
    }

    public void loadLootTables() {
        lootTables.clear();
        FileConfiguration config = plugin.getLootTablesConfig();
        ConfigurationSection lootTablesSection = config.getConfigurationSection("loottables");
        if (lootTablesSection == null) {
            return;
        }

        for (String entityTypeName : lootTablesSection.getKeys(false)) {
            try {
                EntityType entityType = EntityType.valueOf(entityTypeName.toUpperCase());
                ConfigurationSection itemsSection = lootTablesSection.getConfigurationSection(entityTypeName);
                if (itemsSection == null) continue;

                Map<Material, Integer> drops = new HashMap<>();
                for (String materialName : itemsSection.getKeys(false)) {
                    try {
                        Material material = Material.valueOf(materialName.toUpperCase());
                        int amount = itemsSection.getInt(materialName);
                        drops.put(material, amount);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in loot table: " + materialName);
                    }
                }
                lootTables.put(entityType, drops);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type in loot table: " + entityTypeName);
            }
        }
    }

    public Map<Material, Integer> getLoot(EntityType entityType) {
        return lootTables.get(entityType);
    }
}
