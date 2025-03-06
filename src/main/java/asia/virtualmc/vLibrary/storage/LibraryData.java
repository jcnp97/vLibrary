package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.VLibrary;
import asia.virtualmc.vLibrary.configs.DatabaseConfig;
import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class
LibraryData implements Listener {
    private static HikariDataSource dataSource;
    private final VLibrary plugin;

    public LibraryData(VLibrary plugin) {
        this.plugin = plugin;
        if (!connectToDatabase()) {
            plugin.getLogger().severe("Cannot connect to your database. Disabling plugin..");
            getServer().getPluginManager().disablePlugin(plugin);
            return;
        }
        createTable();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private boolean connectToDatabase() {
        try {
            DatabaseConfig.readDatabaseFile(plugin);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + DatabaseConfig.getHost() + ":" + DatabaseConfig.getPort() + "/" + DatabaseConfig.getDatabaseName());
            config.setUsername(DatabaseConfig.getUsername());
            config.setPassword(DatabaseConfig.getPassword());
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(5);
            config.setIdleTimeout(300000);
            config.setConnectionTimeout(10000);
            config.setMaxLifetime(1800000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            try (Connection connection = dataSource.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    ConsoleMessageUtil.pluginPrint("Successfully connected to the MySQL database.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Error during database setup: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS vlib_players (" +
                "playerID INT NOT NULL AUTO_INCREMENT, " +
                "uuid CHAR(36) NOT NULL, " +
                "PRIMARY KEY (playerID), " +
                "UNIQUE KEY (uuid)" +
                ")";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
            ConsoleMessageUtil.pluginPrint("Table 'vlib_players' checked/created successfully.");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error creating table: " + e.getMessage());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Integer playerId = getPlayerID(event.getPlayer().getUniqueId());
    }

    public Integer getPlayerID(UUID uuid) {
        // Inserts the player if not already present and returns the playerID using LAST_INSERT_ID().
        String query = "INSERT INTO vlib_players (uuid) VALUES (?) " +
                "ON DUPLICATE KEY UPDATE playerID = LAST_INSERT_ID(playerID)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error fetching/inserting playerID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Replaces the UUID for the given playerID.
     *
     * @param playerID the player's unique database ID.
     * @param newUuid  the new UUID to be set.
     * @return true if the update was successful, false otherwise.
     */
    public boolean replaceUUID(int playerID, UUID newUuid) {
        String updateQuery = "UPDATE vlib_players SET uuid = ? WHERE playerID = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateQuery)) {
            statement.setString(1, newUuid.toString());
            statement.setInt(2, playerID);
            int affectedRows = statement.executeUpdate();
            return affectedRows == 1;
        } catch (SQLException e) {
            plugin.getLogger().severe("Error updating uuid for playerID " + playerID + ": " + e.getMessage());
            return false;
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void closeConnection() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
