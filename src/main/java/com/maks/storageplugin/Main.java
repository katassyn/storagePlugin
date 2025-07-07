package com.maks.storageplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Initialize configuration
        configManager = new ConfigManager(this);

        // Initialize database
        databaseManager = new DatabaseManager(this);
        databaseManager.connect();

        // Register command
        getCommand("storage").setExecutor(new StorageCommand());

        // Register listeners
        getServer().getPluginManager().registerEvents(new StorageGUI(), this);
        getServer().getPluginManager().registerEvents(new StoragePageListener(this), this);
    }

    @Override
    public void onDisable() {
        // Close database connection
        databaseManager.disconnect();
    }

    public static Main getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
