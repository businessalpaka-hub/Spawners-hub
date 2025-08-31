package com.jules.stackablespawners;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HopperManager {

    private final StackableSpawners plugin;
    private final Map<Location, HopperData> hopperDataMap = new HashMap<>();
    private File hoppersFile;
    private FileConfiguration hoppersConfig;

    public HopperManager(StackableSpawners plugin) {
        this.plugin = plugin;
        loadHoppers();
    }

    public void addHopper(Location loc, UUID owner, double multiplier, String name) {
        hopperDataMap.put(loc, new HopperData(owner, multiplier, name));
        saveHoppers();
    }

    public void removeHopper(Location loc) {
        hopperDataMap.remove(loc);
        saveHoppers();
    }

    public boolean isMultiplierHopper(Location loc) {
        return hopperDataMap.containsKey(loc);
    }

    public HopperData getHopperData(Location loc) {
        return hopperDataMap.get(loc);
    }

    @SuppressWarnings("unchecked")
    public void loadHoppers() {
        hoppersFile = new File(plugin.getDataFolder(), "hoppers.yml");
        if (!hoppersFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                hoppersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create hoppers.yml!");
                e.printStackTrace();
            }
        }
        hoppersConfig = YamlConfiguration.loadConfiguration(hoppersFile);
        if (hoppersConfig.isList("hoppers")) {
            List<Map<?, ?>> hopperList = hoppersConfig.getMapList("hoppers");
            for (Map<?, ?> hopperMap : hopperList) {
                if (hopperMap.get("location") instanceof Map) {
                    Location loc = Location.deserialize((Map<String, Object>) hopperMap.get("location"));
                    UUID owner = UUID.fromString((String) hopperMap.get("owner"));
                    double multiplier = (Double) hopperMap.get("multiplier");
                    String name = (String) hopperMap.get("name");
                    hopperDataMap.put(loc, new HopperData(owner, multiplier, name));
                }
            }
        }
    }

    public void saveHoppers() {
        if (hoppersConfig == null) {
            hoppersFile = new File(plugin.getDataFolder(), "hoppers.yml");
            hoppersConfig = YamlConfiguration.loadConfiguration(hoppersFile);
        }
        List<Map<String, Object>> hopperList = new ArrayList<>();
        for (Map.Entry<Location, HopperData> entry : hopperDataMap.entrySet()) {
            Map<String, Object> hopperMap = new HashMap<>();
            hopperMap.put("location", entry.getKey().serialize());
            hopperMap.put("owner", entry.getValue().getOwner().toString());
            hopperMap.put("multiplier", entry.getValue().getMultiplier());
            hopperMap.put("name", entry.getValue().getName());
            hopperList.add(hopperMap);
        }
        hoppersConfig.set("hoppers", hopperList);
        try {
            hoppersConfig.save(hoppersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save hoppers to file!");
            e.printStackTrace();
        }
    }

    public static class HopperData {
        private final UUID owner;
        private final double multiplier;
        private final String name;

        public HopperData(UUID owner, double multiplier, String name) {
            this.owner = owner;
            this.multiplier = multiplier;
            this.name = name;
        }

        public UUID getOwner() {
            return owner;
        }

        public double getMultiplier() {
            return multiplier;
        }

        public String getName() {
            return name;
        }
    }
}
