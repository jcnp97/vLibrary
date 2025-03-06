//package asia.virtualmc.vLibrary.storage;
//
//import org.bukkit.Bukkit;
//import org.bukkit.plugin.Plugin;
//import org.jetbrains.annotations.NotNull;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class DoubleDataLib {
//    private final Plugin plugin;
//    private final DatabaseLib databaseLib;
//
//    public DoubleDataLib(@NotNull StorageManagerLib storageManager) {
//        this.plugin = storageManager.getMain();
//        this.databaseLib = storageManager.getDatabaseLib();
//    }
//
//    public void createTable(@NotNull List<String> statList, @NotNull String tableName, String prefix) {
//        try (Connection conn = databaseLib.getDataSource().getConnection()) {
//            // Create data definition table
//            conn.createStatement().execute(
//                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
//                            "dataID INT NOT NULL AUTO_INCREMENT," +
//                            "dataName VARCHAR(255) NOT NULL," +
//                            "PRIMARY KEY (dataID)" +
//                            ")"
//            );
//
//            // Create player data table with foreign key - amount changed from INT to DOUBLE
//            conn.createStatement().execute(
//                    "CREATE TABLE IF NOT EXISTS " + tableName + "_data (" +
//                            "UUID VARCHAR(36) NOT NULL," +
//                            "dataID INT NOT NULL," +
//                            "amount DOUBLE DEFAULT 0," +
//                            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
//                            "PRIMARY KEY (UUID, dataID)," +
//                            "FOREIGN KEY (dataID) REFERENCES " + tableName + "(dataID)" +
//                            "ON DELETE CASCADE ON UPDATE CASCADE" +
//                            ")"
//            );
//
//            // Insert new stat types if they don't exist
//            String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE dataName = ?";
//            String insertQuery = "INSERT INTO " + tableName + " (dataName) VALUES (?)";
//
//            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
//                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
//                for (String data : statList) {
//                    checkStmt.setString(1, data);
//                    try (ResultSet rs = checkStmt.executeQuery()) {
//                        if (rs.next() && rs.getInt(1) == 0) {
//                            insertStmt.setString(1, data);
//                            insertStmt.executeUpdate();
//                        }
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            Bukkit.getLogger().severe(prefix + "Failed to create " + tableName + " tables: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public void savePlayerData(@NotNull UUID uuid, @NotNull String tableName,
//                               @NotNull Map<Integer, Double> playerData, String prefix) {
//        if (playerData.isEmpty()) {
//            Bukkit.getLogger().warning(prefix + "Attempted to update data for player " +
//                    uuid + " but the provided data map is empty.");
//            return;
//        }
//
//        String sql = "INSERT INTO " + tableName + "_data (UUID, dataID, amount) VALUES (?, ?, ?) " +
//                "ON DUPLICATE KEY UPDATE amount = VALUES(amount)";
//
//        try (Connection conn = databaseLib.getDataSource().getConnection()) {
//            conn.setAutoCommit(false);
//            try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                for (Map.Entry<Integer, Double> entry : playerData.entrySet()) {
//                    ps.setString(1, uuid.toString());
//                    ps.setInt(2, entry.getKey());
//                    ps.setDouble(3, entry.getValue());
//                    ps.addBatch();
//                }
//                ps.executeBatch();
//                conn.commit();
//            } catch (SQLException e) {
//                conn.rollback();
//                throw e;
//            }
//        } catch (SQLException e) {
//            Bukkit.getLogger().severe(prefix + "Failed to update data on " + tableName + " for player " +
//                    uuid + ": " + e.getMessage());
//        }
//    }
//
//    public void saveAllData(@NotNull String tableName,
//                            @NotNull ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Double>> allPlayerData,
//                            String prefix) {
//        if (allPlayerData.isEmpty()) {
//            //Bukkit.getLogger().info(prefix + "No player data to save during bulk save operation.");
//            return;
//        }
//
//        String sql = "INSERT INTO " + tableName + "_data (UUID, dataID, amount) VALUES (?, ?, ?) " +
//                "ON DUPLICATE KEY UPDATE amount = VALUES(amount)";
//
//        try (Connection conn = databaseLib.getDataSource().getConnection()) {
//            conn.setAutoCommit(false);
//            try (PreparedStatement ps = conn.prepareStatement(sql)) {
//                int batchCount = 0;
//                final int BATCH_SIZE = 1000;
//
//                for (Map.Entry<UUID, ConcurrentHashMap<Integer, Double>> playerEntry : allPlayerData.entrySet()) {
//                    String playerUUID = playerEntry.getKey().toString();
//                    for (Map.Entry<Integer, Double> dataEntry : playerEntry.getValue().entrySet()) {
//                        ps.setString(1, playerUUID);
//                        ps.setInt(2, dataEntry.getKey());
//                        ps.setDouble(3, dataEntry.getValue());
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
//
//    public void createNewPlayerData(@NotNull UUID uuid, @NotNull String tableName, String prefix) {
//        try (Connection conn = databaseLib.getDataSource().getConnection()) {
//            conn.setAutoCommit(false);
//
//            String insertQuery =
//                    "INSERT INTO " + tableName + "_data (UUID, dataID, amount) " +
//                            "SELECT ?, ?, 0.0 " +
//                            "WHERE NOT EXISTS (SELECT 1 FROM " + tableName + "_data WHERE UUID = ? AND dataID = ?)";
//
//            try (PreparedStatement talentStmt = conn.prepareStatement("SELECT dataID FROM " + tableName);
//                 ResultSet rs = talentStmt.executeQuery();
//                 PreparedStatement ps = conn.prepareStatement(insertQuery)) {
//
//                while (rs.next()) {
//                    int currentDataID = rs.getInt("dataID");
//                    ps.setString(1, uuid.toString());
//                    ps.setInt(2, currentDataID);
//                    ps.setString(3, uuid.toString());  // For the WHERE NOT EXISTS clause
//                    ps.setInt(4, currentDataID);       // For the WHERE NOT EXISTS clause
//                    ps.addBatch();
//                }
//
//                ps.executeBatch();
//                conn.commit();
//
//            } catch (SQLException e) {
//                conn.rollback();
//                throw e;
//            }
//        } catch (SQLException e) {
//            Bukkit.getLogger().severe(prefix + "Failed to create new player data on " + tableName +
//                    " for " + uuid + ": " + e.getMessage());
//        }
//    }
//
//    public ConcurrentHashMap<Integer, Double> loadPlayerData(@NotNull UUID uuid,
//                                                             @NotNull String tableName,
//                                                             String prefix) {
//        ConcurrentHashMap<Integer, Double> playerDataMap = new ConcurrentHashMap<>();
//
//        try (Connection conn = databaseLib.getDataSource().getConnection()) {
//            // First check if player data exists
//            PreparedStatement checkPs = conn.prepareStatement(
//                    "SELECT COUNT(*) FROM " + tableName + "_data WHERE UUID = ?"
//            );
//            checkPs.setString(1, uuid.toString());
//            ResultSet checkRs = checkPs.executeQuery();
//
//            if (!checkRs.next() || checkRs.getInt(1) == 0) {
//                // Create new player data if it doesn't exist
//                createNewPlayerData(uuid, tableName, prefix);
//            }
//
//            // Load the data (whether it was just created or already existed)
//            PreparedStatement loadPs = conn.prepareStatement(
//                    "SELECT dataID, amount FROM " + tableName + "_data WHERE UUID = ?"
//            );
//            loadPs.setString(1, uuid.toString());
//            ResultSet loadRs = loadPs.executeQuery();
//
//            while (loadRs.next()) {
//                int dataID = loadRs.getInt("dataID");
//                double amount = loadRs.getDouble("amount");
//                playerDataMap.put(dataID, amount);
//            }
//        } catch (SQLException e) {
//            Bukkit.getLogger().severe(prefix + "Failed to load data from " + tableName + " for player " +
//                    uuid + ": " + e.getMessage());
//        }
//
//        return playerDataMap;
//    }
//}