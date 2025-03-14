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

public class IntegerDataLib {
    private final LibraryData libraryData;
    private final ConcurrentHashMap<UUID, Integer> playerIdCache = new ConcurrentHashMap<>();
    private static final int BATCH_SIZE = 1000;

    public IntegerDataLib(@NotNull StorageManagerLib storageManager) {
        this.libraryData = storageManager.getLibraryData();
    }

    /**
     * Creates necessary tables for data storage.
     * <p>
     * Note: The creation of the players table has been removed because vlib_players is now managed externally.
     * The player data table now references vlib_players.
     */
    public void createTable(@NotNull List<String> statList, @NotNull String tableName, String prefix) {
        try (Connection conn = libraryData.getDataSource().getConnection()) {
            // Create data definition table
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "data_id INT NOT NULL AUTO_INCREMENT," +
                            "data_name VARCHAR(255) NOT NULL," +
                            "PRIMARY KEY (data_id)," +
                            "UNIQUE KEY (data_name)" +
                            ")"
            );

            // Create player data table with composite foreign keys (now referencing vlib_players)
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS " + tableName + "_data (" +
                            "player_id INT NOT NULL," +
                            "data_id INT NOT NULL," +
                            "amount INT DEFAULT 0," +
                            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (player_id, data_id)," +
                            "FOREIGN KEY (player_id) REFERENCES vlib_players(playerID) ON DELETE CASCADE," +
                            "FOREIGN KEY (data_id) REFERENCES " + tableName + "(data_id) ON DELETE CASCADE," +
                            "INDEX idx_player_id (player_id)" +
                            ")"
            );

            // Insert new stat types if they don't exist
            String insertQuery = "INSERT IGNORE INTO " + tableName + " (data_name) VALUES (?)";

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                for (String data : statList) {
                    insertStmt.setString(1, data);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create " + tableName + " tables: " + e.getMessage());
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
     */
    public void savePlayerData(@NotNull UUID uuid, @NotNull String tableName,
                               @NotNull Map<Integer, Integer> playerData, String prefix) {
        if (playerData.isEmpty()) {
            Bukkit.getLogger().warning(prefix + "Attempted to update data for player " +
                    uuid + " but the provided data map is empty.");
            return;
        }

        String sql = "INSERT INTO " + tableName + "_data (player_id, data_id, amount) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = VALUES(amount)";

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Map.Entry<Integer, Integer> entry : playerData.entrySet()) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, entry.getKey());
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
     */
    public void saveAllData(@NotNull String tableName,
                            @NotNull ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Integer>> allPlayerData,
                            String prefix) {
        if (allPlayerData.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO " + tableName + "_data (player_id, data_id, amount) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE amount = VALUES(amount)";

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int batchCount = 0;

                for (Map.Entry<UUID, ConcurrentHashMap<Integer, Integer>> playerEntry : allPlayerData.entrySet()) {
                    int playerId = getOrCreatePlayerId(playerEntry.getKey(), conn, prefix);

                    for (Map.Entry<Integer, Integer> dataEntry : playerEntry.getValue().entrySet()) {
                        ps.setInt(1, playerId);
                        ps.setInt(2, dataEntry.getKey());
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
     */
    public void createNewPlayerData(@NotNull UUID uuid, @NotNull String tableName, String prefix) {
        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            String insertQuery =
                    "INSERT INTO " + tableName + "_data (player_id, data_id, amount) " +
                            "SELECT ?, data_id, 0 FROM " + tableName + " t " +
                            "WHERE NOT EXISTS (SELECT 1 FROM " + tableName + "_data " +
                            "WHERE player_id = ? AND data_id = t.data_id)";

            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setInt(1, playerId);
                ps.setInt(2, playerId);
                ps.executeUpdate();
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
     */
    public ConcurrentHashMap<Integer, Integer> loadPlayerData(@NotNull UUID uuid,
                                                              @NotNull String tableName,
                                                              String prefix) {
        ConcurrentHashMap<Integer, Integer> playerDataMap = new ConcurrentHashMap<>();

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            // Retrieve playerID using the external vlib_players table
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            // Check if player data exists
            String countQuery = "SELECT COUNT(*) FROM " + tableName + "_data WHERE player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(countQuery)) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) == 0) {
                        // Create new player data if it doesn't exist
                        createNewPlayerData(uuid, tableName, prefix);
                    }
                }
            }

            // Load the data
            String loadQuery = "SELECT d.data_id, d.amount FROM " + tableName + "_data d " +
                    "WHERE d.player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(loadQuery)) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int dataId = rs.getInt("data_id");
                        int amount = rs.getInt("amount");
                        playerDataMap.put(dataId, amount);
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
