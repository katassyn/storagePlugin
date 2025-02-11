package com.maks.storageplugin;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.Base64;
import java.util.UUID;

public class PlayerStorage {

    private final UUID playerUUID;
    private final Main plugin;
    private int maxPages;

    public PlayerStorage(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.plugin = Main.getInstance();
        this.maxPages = loadMaxPages();
    }

    private int loadMaxPages() {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT max_pages FROM player_data WHERE uuid=?")) {

            stmt.setString(1, playerUUID.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("max_pages");
                } else {
                    // Insert a new record if not exists
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO player_data (uuid, max_pages) VALUES (?, ?)")) {
                        insertStmt.setString(1, playerUUID.toString());
                        insertStmt.setInt(2, 1); // Default to 1 page
                        insertStmt.executeUpdate();
                        return 1;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    public void saveMaxPages() {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE player_data SET max_pages=? WHERE uuid=?")) {

            stmt.setInt(1, maxPages);
            stmt.setString(2, playerUUID.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Inventory getPage(int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Storage - Page " + page);

        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT data FROM player_storage WHERE uuid=? AND page=?")) {

            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, page);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String data = rs.getString("data");
                    if (data != null && !data.isEmpty()) {
                        ItemStack[] items = deserializeInventory(data);
                        // Set the item slots (0 to 44)
                        for (int i = 0; i <= 44 && i < items.length; i++) {
                            inventory.setItem(i, items[i]);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventory;
    }

    public void savePage(int page, Inventory inventory) {
        try (Connection conn = plugin.getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "REPLACE INTO player_storage (uuid, page, data) VALUES (?, ?, ?)")) {

            stmt.setString(1, playerUUID.toString());
            stmt.setInt(2, page);

            // Get the item slots (0 to 44)
            ItemStack[] items = new ItemStack[45];
            for (int i = 0; i <= 44; i++) {
                items[i] = inventory.getItem(i);
            }
            String data = serializeInventory(items);
            stmt.setString(3, data);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getMaxPages() {
        return maxPages;
    }

    public void setMaxPages(int maxPages) {
        this.maxPages = maxPages;
        saveMaxPages();
    }

    private ItemStack[] deserializeInventory(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int length = dataInput.readInt();
            ItemStack[] items = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize inventory.", e);
        }
    }

    private String serializeInventory(ItemStack[] items) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (ItemStack item : items) {
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize inventory.", e);
        }
    }
}