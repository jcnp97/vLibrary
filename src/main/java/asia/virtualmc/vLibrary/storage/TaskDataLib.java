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

public class TaskDataLib {
    private final LibraryData libraryData;
    private final ConcurrentHashMap<UUID, Integer> playerIdCache = new ConcurrentHashMap<>();
    // Store the default task list for use when creating new player data
    private List<String> defaultTaskList;
    private static final int BATCH_SIZE = 1000;

    public TaskDataLib(@NotNull StorageManagerLib storageManager) {
        this.libraryData = storageManager.getLibraryData();
    }

    public static class TaskDetails {
        public String hashedString;
        public long expiration;

        public TaskDetails(String hashedString, long expiration) {
            this.hashedString = hashedString;
            this.expiration = expiration;
        }
    }

    /**
     * Creates the player data table.
     * <p>
     * Note: The definition table has been removed. Instead, the player data table now contains:
     * player_id, task_name, task_required, and expiration_time.
     */
    public void createTable(@NotNull List<String> taskList, @NotNull String tableName, String prefix) {
        // Cache the default tasks so that new players can be initialized
        this.defaultTaskList = taskList;

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            // Create the player data table with task_name, task_required, and expiration_time
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS " + tableName + "_data (" +
                            "player_id INT NOT NULL," +
                            "task_name VARCHAR(255) NOT NULL," +
                            "task_required VARCHAR(255) DEFAULT ''," +
                            "expiration_time BIGINT DEFAULT 0," +
                            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                            "PRIMARY KEY (player_id, task_name)," +
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
     * Note: The playerData map now uses TaskDetails as values.
     */
    public void savePlayerData(@NotNull UUID uuid, @NotNull String tableName,
                               @NotNull Map<String, TaskDetails> playerData, String prefix) {
        if (playerData.isEmpty()) {
            Bukkit.getLogger().warning(prefix + "Attempted to update data for player " +
                    uuid + " but the provided data map is empty.");
            return;
        }

        String sql = "INSERT INTO " + tableName + "_data (player_id, task_name, task_required, expiration_time) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE task_required = VALUES(task_required), expiration_time = VALUES(expiration_time)";

        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Map.Entry<String, TaskDetails> entry : playerData.entrySet()) {
                    ps.setInt(1, playerId);
                    ps.setString(2, entry.getKey());
                    ps.setString(3, entry.getValue().hashedString);
                    ps.setLong(4, entry.getValue().expiration);
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

//    /**
//     * Saves data for all players in bulk.
//     * <p>
//     * Note: The inner map now uses TaskDetails as values.
//     */
//    public void saveAllData(@NotNull String tableName,
//                            @NotNull ConcurrentHashMap<UUID, ConcurrentHashMap<String, TaskDetails>> allPlayerData,
//                            String prefix) {
//        if (allPlayerData.isEmpty()) {
//            return;
//        }
//
//        String sql = "INSERT INTO " + tableName + "_data (player_id, task_name, task_required, expiration_time) VALUES (?, ?, ?, ?) " +
//                "ON DUPLICATE KEY UPDATE task_required = VALUES(task_required), expiration_time = VALUES(expiration_time)";
//
//        try (Connection conn = libraryData.getDataSource().getConnection()) {
//            conn.setAutoCommit(false);
//            try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                int batchCount = 0;
//
//                for (Map.Entry<UUID, ConcurrentHashMap<String, TaskDetails>> playerEntry : allPlayerData.entrySet()) {
//                    int playerId = getOrCreatePlayerId(playerEntry.getKey(), conn, prefix);
//
//                    for (Map.Entry<String, TaskDetails> dataEntry : playerEntry.getValue().entrySet()) {
//                        ps.setInt(1, playerId);
//                        ps.setString(2, dataEntry.getKey());
//                        ps.setString(3, dataEntry.getValue().hashedString);
//                        ps.setLong(4, dataEntry.getValue().expiration);
//                        ps.addBatch();
//
//                        batchCount++;
//                        if (batchCount >= BATCH_SIZE) {
//                            ps.executeBatch();
//                            batchCount = 0;
//                        }
//                    }
//                }
//
//                if (batchCount > 0) {
//                    ps.executeBatch();
//                }
//
//                conn.commit();
//            } catch (SQLException e) {
//                conn.rollback();
//                throw e;
//            }
//        } catch (SQLException e) {
//            Bukkit.getLogger().severe(prefix + "Failed to perform bulk data save on " + tableName +
//                    ": " + e.getMessage());
//        }
//    }

    /**
     * Creates default data entries for a new player.
     * <p>
     * Instead of selecting default task types from a definition table, we now loop through the
     * cached default task list and insert a row with an empty string for task_required and 0 for expiration_time for each task.
     */
    public void createNewPlayerData(@NotNull UUID uuid, @NotNull String tableName, String prefix) {
        if (defaultTaskList == null || defaultTaskList.isEmpty()) {
            Bukkit.getLogger().warning(prefix + "Default task list is empty, skipping creation of new player data for " + uuid);
            return;
        }
        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            int playerId = getOrCreatePlayerId(uuid, conn, prefix);

            String insertQuery =
                    "INSERT IGNORE INTO " + tableName + "_data (player_id, task_name, task_required, expiration_time) VALUES (?, ?, '', 0)";
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                for (String task : defaultTaskList) {
                    ps.setInt(1, playerId);
                    ps.setString(2, task);
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
     * Note: The result map now uses TaskDetails as values.
     */
    public ConcurrentHashMap<String, TaskDetails> loadPlayerData(@NotNull UUID uuid,
                                                                 @NotNull String tableName,
                                                                 String prefix) {
        ConcurrentHashMap<String, TaskDetails> playerDataMap = new ConcurrentHashMap<>();

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

            // Load the data using task_name as key and TaskDetails as value
            String loadQuery = "SELECT task_name, task_required, expiration_time FROM " + tableName + "_data WHERE player_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(loadQuery)) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String taskName = rs.getString("task_name");
                        String taskRequired = rs.getString("task_required");
                        long expirationTime = rs.getLong("expiration_time");
                        playerDataMap.put(taskName, new TaskDetails(taskRequired, expirationTime));
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