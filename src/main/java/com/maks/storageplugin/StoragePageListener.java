package com.maks.storageplugin;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StoragePageListener implements Listener {

    private final Main plugin;

    public StoragePageListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right click interactions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) {
            return;
        }

        // Check if this is a Storage Page item
        if (isStoragePage(item)) {
            event.setCancelled(true); // Prevent normal interaction

            // Check if player already has max pages
            PlayerStorage storage = new PlayerStorage(player.getUniqueId());
            int maxAllowedPages = plugin.getConfigManager().getMaxPages();

            if (storage.getMaxPages() >= maxAllowedPages) {
                player.sendMessage(ChatColor.RED + "You have reached the maximum number of storage pages.");
                return;
            }

            // Add new page
            int newPageCount = storage.getMaxPages() + 1;
            storage.setMaxPages(newPageCount);

            // Consume one item
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.getInventory().setItemInMainHand(null);
            }

            player.sendMessage(ChatColor.GREEN + "You have unlocked a new storage page! " +
                    "You now have " + newPageCount + " pages.");
        }
    }

    private boolean isStoragePage(ItemStack item) {
        // Check material (potato based on the ID in config)
        if (item.getType() != Material.POTATO) {
            return false;
        }

        if (!item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        
        // Check display name (§3 is ChatColor.DARK_AQUA)
        if (!meta.hasDisplayName() || !meta.getDisplayName().equals(ChatColor.DARK_AQUA + "Storage Page")) {
            return false;
        }
        
        // Check enchantment
        if (!meta.hasEnchant(Enchantment.DURABILITY) || meta.getEnchantLevel(Enchantment.DURABILITY) != 10) {
            return false;
        }
        
        // Check lore
        if (!meta.hasLore() || meta.getLore().isEmpty()) {
            return false;
        }
        
        // The lore contains the text about right-clicking
        String firstLoreLine = meta.getLore().get(0);
        return firstLoreLine.contains("Right click to get one storage page");
    }
}