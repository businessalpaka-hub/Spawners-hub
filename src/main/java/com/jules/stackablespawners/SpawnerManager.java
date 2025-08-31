package com.jules.stackablespawners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SpawnerManager {

    private final StackableSpawners plugin;
    private final NamespacedKey stackSizeKey;
    private final Set<Location> spawnerLocations = new HashSet<>();
    private File spawnersFile;
    private FileConfiguration spawnersConfig;

    public SpawnerManager(StackableSpawners plugin) {
        this.plugin = plugin;
        this.stackSizeKey = new NamespacedKey(plugin, "spawner_stack_size");
        loadSpawners();
    }

    public boolean isSpawner(Block block) {
        return block.getType() == Material.SPAWNER;
    }

    public int getStackSize(Block block) {
        if (!isSpawner(block)) {
            return 0;
        }
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        PersistentDataContainer container = spawner.getPersistentDataContainer();
        return container.getOrDefault(stackSizeKey, PersistentDataType.INTEGER, 1);
    }

    public void setStackSize(Block block, int size) {
        if (!isSpawner(block)) {
            return;
        }
        CreatureSpawner spawner = (CreatureSpawner) block.getState();
        PersistentDataContainer container = spawner.getPersistentDataContainer();
        container.set(stackSizeKey, PersistentDataType.INTEGER, size);
        spawner.update();

        if (size > 1 && !spawnerLocations.contains(block.getLocation())) {
            spawnerLocations.add(block.getLocation());
        } else if (size <= 1 && spawnerLocations.contains(block.getLocation())) {
            spawnerLocations.remove(block.getLocation());
        }
    }

    public void addSpawner(Location loc) {
        spawnerLocations.add(loc);
    }

    public void removeSpawner(Location loc) {
        spawnerLocations.remove(loc);
    }

    public Set<Location> getSpawnerLocations() {
        return new HashSet<>(spawnerLocations);
    }

    @SuppressWarnings("unchecked")
    public void loadSpawners() {
        spawnersFile = new File(plugin.getDataFolder(), "spawners.yml");
        if (!spawnersFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                spawnersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create spawners.yml!");
                e.printStackTrace();
            }
        }
        spawnersConfig = YamlConfiguration.loadConfiguration(spawnersFile);
        if (spawnersConfig.isList("spawners")) {
            List<?> locList = spawnersConfig.getList("spawners");
            if(locList == null) return;

            for (Object locObject : locList) {
                if (locObject instanceof Map) {
                    spawnerLocations.add(Location.deserialize((Map<String, Object>) locObject));
                }
            }
        }
    }

    public void saveSpawners() {
        if (spawnersConfig == null) {
            spawnersFile = new File(plugin.getDataFolder(), "spawners.yml");
            spawnersConfig = YamlConfiguration.loadConfiguration(spawnersFile);
        }
        spawnersConfig.set("spawners", spawnerLocations.stream().map(Location::serialize).collect(Collectors.toList()));
        try {
            spawnersConfig.save(spawnersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawners to file!");
            e.printStackTrace();
        }
    }
}
