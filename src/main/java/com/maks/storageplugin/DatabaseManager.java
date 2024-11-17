package com.maks.storageplugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private final Main plugin;
    private Connection connection;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        String host = plugin.getConfig().getString("database.host");
        String port = plugin.getConfig().getString("database.port");
        String database = plugin.getConfig().getString("database.name");
        String user = plugin.getConfig().getString("database.user");
        String password = plugin.getConfig().getString("database.password");

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true", user, password);
            createTables();
            plugin.getLogger().info("Connected to MySQL database.");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to connect to MySQL database.");
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Disconnected from MySQL database.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createTables() throws SQLException {
        String storageTable = "CREATE TABLE IF NOT EXISTS player_storage (" +
                "uuid VARCHAR(36) NOT NULL," +
                "page INT NOT NULL," +
                "data TEXT," +
                "PRIMARY KEY (uuid, page)" +
                ");";
        PreparedStatement storageStmt = connection.prepareStatement(storageTable);
        storageStmt.execute();
        storageStmt.close();

        String playerDataTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "max_pages INT DEFAULT 1" +
                ");";
        PreparedStatement playerDataStmt = connection.prepareStatement(playerDataTable);
        playerDataStmt.execute();
        playerDataStmt.close();
    }


    public Connection getConnection() {
        return connection;
    }
}
