package asia.virtualmc.vLibrary.storage;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatisticsDataLib {
    private final LibraryData libraryData;
    private final ConcurrentHashMap<UUID, Integer> playerIdCache = new ConcurrentHashMap<>();
    // Store the default stat list for use when creating new player data
    private List<String> defaultStatList;
    private static final int BATCH_SIZE = 1000;

    public StatisticsDataLib(@NotNull StorageManagerLib storageManager) {
        this.libraryData = storageManager.getLibraryData();
    }

    /**
     * Creates the player data table.
     * <p>
     * Note: The definition table has been removed. Instead, the player data table now contains:
     * player_id, stat_name, and amount.
     */
    public void createTable(@NotNull List<String> statList, @NotNull String tableName, String prefix) {
        // Cache the default stats so that new players can be initialized
        this.defaultStatList = statList;

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            // Create the player data table with stat_name directly
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS " + tableName + "_data (" +
                            "player_id INT NOT NULL," +
                            "stat_name VARCHAR(255) NOT NULL," +
                            "amount INT DEFAULT 0," +
                            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (player_id, stat_name)," +
                            "FOREIGN KEY (player_id) REFERENCES vlib_players(playerID) ON DELETE CASCADE," +
                            "INDEX idx_player_id (player_id)" +
                            ")"
            );
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create " + tableName + " data table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method that retrieves (and caches) the playerID using getPlayerID.
     * Logs a warning if getPlayerID returns null.
     *
     * @param uuid   the player's UUID
     * @param conn   the current database connection (unused in this implementation but preserved for signature consistency)
     * @param prefix a logging prefix
     * @return the playerID
     * @throws SQLException if the playerID could not be retrieved
     */
    private int getOrCreatePlayerId(UUID uuid, Connection conn, String prefix) throws SQLException {
        if (playerIdCache.containsKey(uuid)) {
            return playerIdCache.get(uuid);
        }
        Integer playerId = libraryData.getPlayerID(uuid);
        if (playerId == null) {
            Bukkit.getLogger().warning(prefix + " getPlayerID returned null for UUID: " + uuid);
            throw new SQLException("Failed to retrieve playerID for UUID: " + uuid);
        }
        playerIdCache.put(uuid, playerId);
        return playerId;
    }

    /**
     * Saves data for a single player.
     * <p>
     * Note: The key of the playerData map is now the stat name.
     */
    public void savePlayerData(@NotNull UUID uuid, @NotNull String tableName,
                               @NotNull Map<String, Integer> playerData, String prefix) {
        if (playerData.isEmpty()) {
            Bukkit.getLogger().warning(prefix + "Attempted to update data for player " +
                    uuid + " but the provided data map is empty.");
            return;
        }

        String sql = "INSERT INTO " + tableName + "_data (player_id, stat_name, amount) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = VALUES(amount)";

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Map.Entry<String, Integer> entry : playerData.entrySet()) {
                    ps.setInt(1, playerId);
                    ps.setString(2, entry.getKey());
                    ps.setInt(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to update data on " + tableName + " for player " +
                    uuid + ": " + e.getMessage());
        }
    }

    /**
     * Saves data for all players in bulk.
     * <p>
     * Note: The inner map now uses stat name as the key.
     */
    public void saveAllData(@NotNull String tableName,
                            @NotNull ConcurrentHashMap<UUID, ConcurrentHashMap<String, Integer>> allPlayerData,
                            String prefix) {
        if (allPlayerData.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO " + tableName + "_data (player_id, stat_name, amount) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = VALUES(amount)";

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int batchCount = 0;

                for (Map.Entry<UUID, ConcurrentHashMap<String, Integer>> playerEntry : allPlayerData.entrySet()) {
                    int playerId = getOrCreatePlayerId(playerEntry.getKey(), conn, prefix);

                    for (Map.Entry<String, Integer> dataEntry : playerEntry.getValue().entrySet()) {
                        ps.setInt(1, playerId);
                        ps.setString(2, dataEntry.getKey());
                        ps.setInt(3, dataEntry.getValue());
                        ps.addBatch();

                        batchCount++;
                        if (batchCount >= BATCH_SIZE) {
                            ps.executeBatch();
                            batchCount = 0;
                        }
                    }
                }

                if (batchCount > 0) {
                    ps.executeBatch();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to perform bulk data save on " + tableName +
                    ": " + e.getMessage());
        }
    }

    /**
     * Creates default data entries for a new player.
     * <p>
     * Instead of selecting default stat types from a definition table, we now loop through the
     * cached default stat list and insert a row with amount 0 for each stat.
     */
    public void createNewPlayerData(@NotNull UUID uuid, @NotNull String tableName, String prefix) {
        if (defaultStatList == null || defaultStatList.isEmpty()) {
            Bukkit.getLogger().warning(prefix + "Default stat list is empty, skipping creation of new player data for " + uuid);
            return;
        }
        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            String insertQuery =
                    "INSERT IGNORE INTO " + tableName + "_data (player_id, stat_name, amount) VALUES (?, ?, 0)";
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                for (String stat : defaultStatList) {
                    ps.setInt(1, playerId);
                    ps.setString(2, stat);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create new player data on " + tableName +
                    " for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Loads data for a single player.
     * <p>
     * Note: The query now retrieves stat_name instead of data_id.
     */
    public ConcurrentHashMap<String, Integer> loadPlayerData(@NotNull UUID uuid,
                                                             @NotNull String tableName,
                                                             String prefix) {
        ConcurrentHashMap<String, Integer> playerDataMap = new ConcurrentHashMap<>();

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            // Retrieve playerID using the external vlib_players table
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            // Check if player data exists; if not, create new default entries
            String countQuery = "SELECT COUNT(*) FROM " + tableName + "_data WHERE player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(countQuery)) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        createNewPlayerData(uuid, tableName, prefix);
                    }
                }
            }

            // Load the data using stat_name as key
            String loadQuery = "SELECT stat_name, amount FROM " + tableName + "_data WHERE player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(loadQuery)) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String statName = rs.getString("stat_name");
                        int amount = rs.getInt("amount");
                        playerDataMap.put(statName, amount);
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to load data from " + tableName + " for player " +
                    uuid + ": " + e.getMessage());
        }

        return playerDataMap;
    }
}
