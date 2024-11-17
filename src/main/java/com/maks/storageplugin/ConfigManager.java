package com.maks.storageplugin;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfigManager {

    private final Main plugin;
    private final int maxPages;
    private final ItemStack currencyItem;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        this.maxPages = config.getInt("max-pages");

        // Create currency item
        String materialName = config.getString("currency-item.material");
        Material material = Material.matchMaterial(materialName);
        String itemName = config.getString("currency-item.name");

        currencyItem = new ItemStack(material);
        ItemMeta meta = currencyItem.getItemMeta();
        meta.setDisplayName(itemName);
        currencyItem.setItemMeta(meta);
    }

    public int getMaxPages() {
        return maxPages;
    }

    public int getPageCost(int page) {
        return plugin.getConfig().getInt("page-costs." + page);
    }

    public ItemStack getCurrencyItem() {
        return currencyItem.clone();
    }
}
