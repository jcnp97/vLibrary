package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.utils.EffectsUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OtherDataLib {
    private final Plugin plugin;
    private final DatabaseLib databaseLib;
    private final EffectsUtil effectsUtil;

    public OtherDataLib(@NotNull StorageManagerLib storageManager) {
        this.plugin = storageManager.getMain();
        this.databaseLib = storageManager.getDatabaseLib();
        this.effectsUtil = storageManager.getEffectsUtil();
    }

    public void createTable(@NotNull List<String> statList, @NotNull String tableName, String prefix) {
        try (Connection conn = databaseLib.getDataSource().getConnection()) {
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "statsID INT NOT NULL AUTO_INCREMENT," +
                            "statsName VARCHAR(255) NOT NULL," +
                            "PRIMARY KEY (statsID)" +
                            ")"
            );
            conn.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "UUID VARCHAR(36) NOT NULL," +
                            "statsID INT NOT NULL," +
                            "amount INT DEFAULT 0," +
                            "PRIMARY KEY (UUID, statsID)," +
                            "FOREIGN KEY (statsID) REFERENCES " + tableName + "(statsID)" +
                            "ON DELETE CASCADE ON UPDATE CASCADE" +
                            ")"
            );

            String checkQuery = "SELECT COUNT(*) FROM " + tableName + " WHERE statsName = ?";
            String insertQuery = "INSERT INTO " + tableName + " (statsName) VALUES (?)";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                for (String stat : statList) {
                    checkStmt.setString(1, stat);
                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next() && rs.getInt(1) == 0) {
                            insertStmt.setString(1, stat);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create statistics table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ConcurrentHashMap<Integer, Integer> getPlayerDataFromDatabase(@NotNull UUID uuid, @NotNull String tableName, String prefix) {
        ConcurrentHashMap<Integer, Integer> playerStatisticsMap = new ConcurrentHashMap<>();

        try (Connection conn = databaseLib.getDataSource().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT statsID, amount FROM " + tableName + " WHERE UUID = ?"
            );
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int statsID = rs.getInt("statsID");
                int amount = rs.getInt("amount");
                playerStatisticsMap.put(statsID, amount);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + " Failed to load statistics data for player " +
                    uuid + ": " + e.getMessage());
        }

        return playerStatisticsMap;
    }

    private void createNewPlayerStats(@NotNull UUID uuid, @NotNull String tableName, String prefix) {
        try (Connection conn = databaseLib.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            PreparedStatement talentStmt = conn.prepareStatement("SELECT statsID FROM " + tableName);
            ResultSet rs = talentStmt.executeQuery();
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO " + tableName + " (UUID, statsID, amount) VALUES (?, ?, 0)"
            );
            while (rs.next()) {
                int currentStatsID = rs.getInt("statsID");
                ps.setString(1, uuid.toString());
                ps.setInt(2, currentStatsID);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create new player statistics for " + uuid + ": " + e.getMessage());
        }
    }

    public void storePlayerData(@NotNull UUID uuid,
                                @NotNull String tableName,
                                @NotNull Map<Integer, Integer> playerData,
                                String prefix) {
        if (playerData.isEmpty()) {
            Bukkit.getLogger().severe(prefix + "Attempted to update data for player " +
                    uuid + " but the provided data map is empty.");
            return;
        }

        String sql = "UPDATE " + tableName + " SET amount = ? WHERE UUID = ? AND statsID = ?";

        try (Connection conn = databaseLib.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Map.Entry<Integer, Integer> entry : playerData.entrySet()) {
                    ps.setInt(1, entry.getValue());
                    ps.setString(2, uuid.toString());
                    ps.setInt(3, entry.getKey());
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to update data for player " +
                    uuid + ": " + e.getMessage());
        }
    }


//    public void updateAllData() {
//        try (Connection conn = databaseLib.getDataSource().getConnection()) {
//            PreparedStatement ps = conn.prepareStatement(
//                    "UPDATE archPlayerStatistics SET amount = ? WHERE UUID = ? AND statsID = ?"
//            );
//            conn.setAutoCommit(false);
//            for (Map.Entry<UUID, ConcurrentHashMap<Integer, Integer>> playerEntry : playerStatistics.entrySet()) {
//                UUID playerUUID = playerEntry.getKey();
//                ConcurrentHashMap<Integer, Integer> talents = playerEntry.getValue();
//                for (Map.Entry<Integer, Integer> talentEntry : talents.entrySet()) {
//                    ps.setInt(1, talentEntry.getValue());
//                    ps.setString(2, playerUUID.toString());
//                    ps.setInt(3, talentEntry.getKey());
//                    ps.addBatch();
//                }
//            }
//            ps.executeBatch();
//            conn.commit();
//        } catch (SQLException e) {
//            Bukkit.getLogger().severe("[vArchaeology] Failed to update statistics data: " + e.getMessage());
//        }
//    }


}
