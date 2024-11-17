package com.maks.storageplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StorageGUI implements Listener {


    public static void openStorage(Player player, PlayerStorage storage, int page) {
        Inventory inventory = storage.getPage(page);

        // Przyciski nawigacyjne
        if (page > 1) {
            ItemStack previousPage = new ItemStack(Material.ARROW);
            ItemMeta meta = previousPage.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Previous Page");
            previousPage.setItemMeta(meta);
            inventory.setItem(45, previousPage);
        }

        if (page < storage.getMaxPages()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta meta = nextPage.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Next Page");
            nextPage.setItemMeta(meta);
            inventory.setItem(53, nextPage);
        }

        // Przycisk zakupu nowej strony
        if (page == storage.getMaxPages() && storage.getMaxPages() < Main.getInstance().getConfigManager().getMaxPages()) {
            ItemStack expand = new ItemStack(Material.EMERALD);
            ItemMeta meta = expand.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Buy New Page");
            int cost = Main.getInstance().getConfigManager().getPageCost(storage.getMaxPages() + 1);
            meta.setLore(java.util.Arrays.asList(ChatColor.YELLOW + "Price: " + cost + " Andermant"));
            expand.setItemMeta(meta);
            inventory.setItem(49, expand);
        }

        // Pozwalamy graczom na korzystanie z pozostałych slotów (w tym dolnego rzędu)

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String title = view.getTitle();
        if (!title.startsWith("Storage - Page ")) {
            return; // Nie nasze GUI
        }

        int rawSlot = event.getRawSlot();
        Player player = (Player) event.getWhoClicked();

        // Sprawdź, czy kliknięcie było w górnym ekwipunku (GUI)
        if (rawSlot < view.getTopInventory().getSize()) {
            ItemStack clickedItem = event.getCurrentItem();

            // Sprawdź, czy kliknięty przedmiot to jeden z przycisków sterujących
            if (clickedItem != null && clickedItem.hasItemMeta()) {
                ItemMeta meta = clickedItem.getItemMeta();
                String itemName = meta.getDisplayName();

                PlayerStorage storage = new PlayerStorage(player.getUniqueId());
                int currentPage = getCurrentPage(title);

                if (itemName.equals(ChatColor.GREEN + "Next Page")) {
                    event.setCancelled(true); // Uniemożliwiamy przenoszenie przycisku
                    storage.savePage(currentPage, event.getInventory());
                    openStorage(player, storage, currentPage + 1);
                } else if (itemName.equals(ChatColor.GREEN + "Previous Page")) {
                    event.setCancelled(true);
                    storage.savePage(currentPage, event.getInventory());
                    openStorage(player, storage, currentPage - 1);
                } else if (itemName.equals(ChatColor.GOLD + "Buy New Page")) {
                    event.setCancelled(true);
                    int maxAllowedPages = Main.getInstance().getConfigManager().getMaxPages();
                    if (storage.getMaxPages() >= maxAllowedPages) {
                        player.sendMessage(ChatColor.RED + "Osiągnąłeś maksymalną liczbę stron.");
                        return;
                    }

                    int newPage = storage.getMaxPages() + 1;
                    int cost = Main.getInstance().getConfigManager().getPageCost(newPage);
                    ItemStack currencyItem = Main.getInstance().getConfigManager().getCurrencyItem();

                    if (hasEnoughCurrency(player, currencyItem, cost)) {
                        removeCurrency(player, currencyItem, cost);
                        storage.setMaxPages(newPage);
                        player.sendMessage(ChatColor.GREEN + "You have purchased a new magazine page!");
                        storage.savePage(currentPage, event.getInventory());
                        openStorage(player, storage, currentPage);
                    } else {
                        player.sendMessage(ChatColor.RED + "You don't have enough Andermant.");
                    }
                }
            } else {
                // Pozwól graczom na interakcję z innymi przedmiotami w GUI
                event.setCancelled(false);
            }
        } else {
            // Kliknięcie było w ekwipunku gracza, pozwól na normalne działanie
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryView view = event.getView();
        String title = view.getTitle();
        if (!title.startsWith("Storage - Page ")) {
            return; // Nie nasze GUI
        }

        // Jeśli jakikolwiek slot przeciągania zawiera przedmioty sterujące, anuluj wydarzenie
        for (Integer slot : event.getRawSlots()) {
            if (slot < view.getTopInventory().getSize()) {
                ItemStack item = view.getItem(slot);
                if (item != null && item.hasItemMeta()) {
                    String itemName = item.getItemMeta().getDisplayName();
                    if (itemName.equals(ChatColor.GREEN + "Next Page") ||
                            itemName.equals(ChatColor.GREEN + "Previous Page") ||
                            itemName.equals(ChatColor.GOLD + "Buy New Page")) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
        // Pozwól na inne przeciągania
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("Storage - Page ")) {
            return; // Nie nasz ekwipunek
        }

        Player player = (Player) event.getPlayer();
        PlayerStorage storage = new PlayerStorage(player.getUniqueId());
        int currentPage = getCurrentPage(title);

        // Zapisz aktualną stronę
        storage.savePage(currentPage, event.getInventory());
    }

    private int getCurrentPage(String inventoryName) {
        String[] parts = inventoryName.split(" ");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private boolean hasEnoughCurrency(Player player, ItemStack currencyItem, int amount) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isCurrencyItem(item, currencyItem)) {
                count += item.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private void removeCurrency(Player player, ItemStack currencyItem, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && isCurrencyItem(item, currencyItem)) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
    }

    private boolean isCurrencyItem(ItemStack item, ItemStack currencyItem) {
        if (item.getType() != currencyItem.getType()) return false;
        if (!item.hasItemMeta() || !currencyItem.hasItemMeta()) return false;
        String itemName = item.getItemMeta().getDisplayName();
        String currencyName = currencyItem.getItemMeta().getDisplayName();
        return itemName.equals(currencyName);
    }
}
