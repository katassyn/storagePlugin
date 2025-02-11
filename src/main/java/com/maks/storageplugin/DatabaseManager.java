package com.maks.storageplugin;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private final Main plugin;
    private HikariDataSource dataSource;

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
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername(user);
            config.setPassword(password);

            // HikariCP settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000); // 5 minutes
            config.setConnectionTimeout(10000); // 10 seconds
            config.setMaxLifetime(1800000); // 30 minutes
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);

            // Test connection and create tables
            try (Connection conn = dataSource.getConnection()) {
                createTables(conn);
            }

            plugin.getLogger().info("Successfully connected to MySQL database using HikariCP.");
        } catch (SQLException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to connect to MySQL database.");
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("Disconnected from MySQL database.");
        }
    }

    private void createTables(Connection connection) throws SQLException {
        String storageTable = "CREATE TABLE IF NOT EXISTS player_storage (" +
                "uuid VARCHAR(36) NOT NULL," +
                "page INT NOT NULL," +
                "data TEXT," +
                "PRIMARY KEY (uuid, page)" +
                ");";
        try (PreparedStatement storageStmt = connection.prepareStatement(storageTable)) {
            storageStmt.execute();
        }

        String playerDataTable = "CREATE TABLE IF NOT EXISTS player_data (" +
                "uuid VARCHAR(36) NOT NULL PRIMARY KEY," +
                "max_pages INT DEFAULT 1" +
                ");";
        try (PreparedStatement playerDataStmt = connection.prepareStatement(playerDataTable)) {
            playerDataStmt.execute();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}