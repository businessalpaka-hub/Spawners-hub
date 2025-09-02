package de.user.sellgui;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class SellGUI extends JavaPlugin {

    private static Economy econ = null;
    private PriceManager priceManager;
    private GUIManager guiManager;

    private File pricesFile;
    private FileConfiguration pricesConfig;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load configs and managers
        reloadConfiguration();

        // Register commands
        getCommand("sell").setExecutor(new SellCommand(this.guiManager));
        getCommand("sellreload").setExecutor(new SellReloadCommand(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new SellGUIListener(this), this);

        getLogger().info("SellGUI has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("SellGUI has been disabled!");
    }

    public void reloadConfiguration() {
        saveDefaultConfig();
        createPricesConfig();

        if (this.priceManager == null) {
            this.priceManager = new PriceManager(this);
        }
        this.priceManager.loadPrices();

        if (this.guiManager == null) {
            this.guiManager = new GUIManager(this);
        }
    }

    private void createPricesConfig() {
        pricesFile = new File(getDataFolder(), "prices.yml");
        if (!pricesFile.exists()) {
            saveResource("prices.yml", false);
        }
        pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);
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

    // Getters
    public Economy getEconomy() {
        return econ;
    }

    public PriceManager getPriceManager() {
        return priceManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public FileConfiguration getPricesConfig() {
        if (pricesConfig == null) {
            createPricesConfig();
        }
        return pricesConfig;
    }
}
