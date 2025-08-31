package com.jules.stackablespawners;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class StackableSpawners extends JavaPlugin {

    private SpawnerManager spawnerManager;
    private HopperManager hopperManager;
    private LootManager lootManager;
    private ItemWorthManager itemWorthManager;

    private File lootTablesFile;
    private FileConfiguration lootTablesConfig;
    private File itemWorthFile;
    private FileConfiguration itemWorthConfig;

    private static Economy econ = null;

    @Override
    public void onEnable() {
        // Setup Vault
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load configs
        saveDefaultConfig();
        createCustomConfigs();

        // Init managers
        this.spawnerManager = new SpawnerManager(this);
        this.hopperManager = new HopperManager(this);
        this.lootManager = new LootManager(this);
        this.itemWorthManager = new ItemWorthManager(this);

        // Load data from configs
        this.lootManager.loadLootTables();
        this.itemWorthManager.loadItemWorth();

        // Register commands
        this.getCommand("sspawner").setExecutor(new SpawnerCommand(this));
        this.getCommand("hopper").setExecutor(new HopperCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new SpawnerListener(this), this);
        getServer().getPluginManager().registerEvents(new HopperListener(this), this);

        // Start task
        long spawnRateTicks = getConfig().getLong("spawners.spawn-rate-seconds", 5) * 20;
        new SpawnerTask(this).runTaskTimer(this, 0L, spawnRateTicks);

        getLogger().info("StackableSpawners has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save data
        spawnerManager.saveSpawners();
        hopperManager.saveHoppers();

        getLogger().info("StackableSpawners has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private void createCustomConfigs() {
        lootTablesFile = new File(getDataFolder(), "loottables.yml");
        if (!lootTablesFile.exists()) {
            saveResource("loottables.yml", false);
        }
        lootTablesConfig = YamlConfiguration.loadConfiguration(lootTablesFile);

        itemWorthFile = new File(getDataFolder(), "itemworth.yml");
        if (!itemWorthFile.exists()) {
            saveResource("itemworth.yml", false);
        }
        itemWorthConfig = YamlConfiguration.loadConfiguration(itemWorthFile);
    }

    // Getters
    public Economy getEconomy() {
        return econ;
    }

    public SpawnerManager getSpawnerManager() {
        return spawnerManager;
    }

    public HopperManager getHopperManager() {
        return hopperManager;
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public ItemWorthManager getItemWorthManager() {
        return itemWorthManager;
    }

    public FileConfiguration getLootTablesConfig() {
        return lootTablesConfig;
    }

    public FileConfiguration getItemWorthConfig() {
        return itemWorthConfig;
    }
}
