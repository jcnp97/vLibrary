package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.enums.EnumsLib;
import asia.virtualmc.vLibrary.utils.DigitUtils;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

public class PlayerDataLib {
    private final Plugin plugin;
    private final DatabaseLib databaseLib;
    //private final EffectsUtil effectsUtil;
    private static final int MAX_EXP = 2_147_483_647;
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 120;

    public PlayerDataLib(@NotNull StorageManagerLib storageManager) {
        this.plugin = storageManager.getMain();
        this.databaseLib = storageManager.getDatabaseLib();
        //this.effectsUtil = storageManager.getEffectsUtil();
    }

    public static class PlayerStats {
        public String name;
        public double exp;
        public double bxp;
        public double xpm;
        public int level;
        public int luck;
        public int traitPoints;
        public int talentPoints;
        public int wisdomTrait;
        public int charismaTrait;
        public int karmaTrait;
        public int dexterityTrait;
        public double data1;
        public double data2;
        public double data3;

        public PlayerStats(String name, double exp, double bxp, double xpm, int level, int luck,
                           int traitPoints, int talentPoints, int wisdomTrait, int charismaTrait,
                           int karmaTrait, int dexterityTrait, double data1, double data2, double data3) {
            this.name = name;
            this.exp = exp;
            this.bxp = bxp;
            this.xpm = xpm;
            this.level = level;
            this.luck = luck;
            this.traitPoints = traitPoints;
            this.talentPoints = talentPoints;
            this.wisdomTrait = wisdomTrait;
            this.charismaTrait = charismaTrait;
            this.karmaTrait = karmaTrait;
            this.dexterityTrait = dexterityTrait;
            this.data1 = data1;
            this.data2 = data2;
            this.data3 = data3;
        }
    }

    public void createTable(@NotNull String tableName, String prefix) {
        try (Connection conn = databaseLib.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS " + tableName +
                             "(playerUUID VARCHAR(36) PRIMARY KEY," +
                             "playerName VARCHAR(16) NOT NULL," +
                             "playerEXP DECIMAL(13,2) DEFAULT 0.00," +
                             "playerBXP DECIMAL(13,2) DEFAULT 0.00," +
                             "playerXPM DECIMAL(4,2) DEFAULT 1.00," +
                             "playerLevel TINYINT DEFAULT 1," +
                             "playerLuck TINYINT DEFAULT 0," +
                             "traitPoints INT DEFAULT 1," +
                             "talentPoints INT DEFAULT 0," +
                             "wisdomTrait INT DEFAULT 0," +
                             "charismaTrait INT DEFAULT 0," +
                             "karmaTrait INT DEFAULT 0," +
                             "dexterityTrait INT DEFAULT 0," +
                             "data1 DECIMAL(5,2) DEFAULT 0.00," +
                             "data2 DECIMAL(5,2) DEFAULT 0.00," +
                             "data3 DECIMAL(5,2) DEFAULT 0.00," +
                             "lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
        ) {
            ps.execute();
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create player data table: " + e.getMessage());
        }
    }

    public void savePlayerData(
            @NotNull UUID uuid,
            @NotNull String name,
            double exp,
            double bxp,
            double xpm,
            int level,
            int luck,
            int traitPoints,
            int talentPoints,
            int wisdom,
            int charisma,
            int karma,
            int dexterity,
            double data1,
            double data2,
            double data3,
            String tableName,
            String prefix
    ) {
        try (Connection conn = databaseLib.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE " + tableName + " SET " +
                             "playerName = ?, playerEXP = ?, playerBXP = ?, " +
                             "playerXPM = ?, playerLevel = ?, playerLuck = ?, " +
                             "traitPoints = ?, talentPoints = ?, wisdomTrait = ?, " +
                             "charismaTrait = ?, karmaTrait = ?, dexterityTrait = ?, " +
                             "data1 = ?, data2 = ?, data3 = ?, lastUpdated = CURRENT_TIMESTAMP " +
                             "WHERE playerUUID = ?")
        ) {
            ps.setString(1, name);
            ps.setDouble(2, exp);
            ps.setDouble(3, bxp);
            ps.setDouble(4, xpm);
            ps.setInt(5, level);
            ps.setInt(6, luck);
            ps.setInt(7, traitPoints);
            ps.setInt(8, talentPoints);
            ps.setInt(9, wisdom);
            ps.setInt(10, charisma);
            ps.setInt(11, karma);
            ps.setInt(12, dexterity);
            ps.setDouble(13, data1);
            ps.setDouble(14, data2);
            ps.setDouble(15, data3);
            ps.setString(16, uuid.toString());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                // If no rows were updated, the player doesn't exist in the database
                createNewPlayerData(uuid, name, tableName, prefix);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to save " + name + " data on database: " + e.getMessage());
        }
    }

    public void saveAllData(
            @NotNull Map<UUID, PlayerStats> playerDataMap,
            String tableName,
            String prefix
    ) {
        if (playerDataMap.isEmpty()) {
            //Bukkit.getLogger().info(prefix + "No player data to save during bulk save operation.");
            return;
        }

        String updateQuery = "UPDATE " + tableName + " SET " +
                "playerName = ?, playerEXP = ?, playerBXP = ?, " +
                "playerXPM = ?, playerLevel = ?, playerLuck = ?, " +
                "traitPoints = ?, talentPoints = ?, wisdomTrait = ?, " +
                "charismaTrait = ?, karmaTrait = ?, dexterityTrait = ?, " +
                "data1 = ?, data2 = ?, data3 = ?, lastUpdated = CURRENT_TIMESTAMP " +
                "WHERE playerUUID = ?";

        try (Connection conn = databaseLib.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {

            conn.setAutoCommit(false);
            int batchSize = 0;

            for (Map.Entry<UUID, PlayerStats> entry : playerDataMap.entrySet()) {
                UUID uuid = entry.getKey();
                PlayerStats stats = entry.getValue();

                ps.setString(1, stats.name);
                ps.setDouble(2, stats.exp);
                ps.setDouble(3, stats.bxp);
                ps.setDouble(4, stats.xpm);
                ps.setInt(5, stats.level);
                ps.setInt(6, stats.luck);
                ps.setInt(7, stats.traitPoints);
                ps.setInt(8, stats.talentPoints);
                ps.setInt(9, stats.wisdomTrait);
                ps.setInt(10, stats.charismaTrait);
                ps.setInt(11, stats.karmaTrait);
                ps.setInt(12, stats.dexterityTrait);
                ps.setDouble(13, stats.data1);
                ps.setDouble(14, stats.data2);
                ps.setDouble(15, stats.data3);
                ps.setString(16, uuid.toString());

                ps.addBatch();
                batchSize++;

                // Execute batch every 100 records
                if (batchSize % 100 == 0) {
                    ps.executeBatch();
                    conn.commit();
                }
            }

            if (batchSize % 100 != 0) {
                ps.executeBatch();
                conn.commit();
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to save all player data: " + e.getMessage());
        }
    }

    public void createNewPlayerData(@NotNull UUID uuid, String name, String tableName, String prefix) {
        try (Connection conn = databaseLib.getDataSource().getConnection()) {
            conn.setAutoCommit(false);

            String insertQuery =
                    "INSERT INTO " + tableName + " (" +
                            "playerUUID, playerName, playerEXP, playerBXP, playerXPM, " +
                            "playerLevel, playerLuck, traitPoints, talentPoints, wisdomTrait, " +
                            "charismaTrait, karmaTrait, dexterityTrait, data1, data2, data3" +
                            ") " +
                            "SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? " +
                            "WHERE NOT EXISTS (SELECT 1 FROM " + tableName + " WHERE playerUUID = ?)";

            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setDouble(3, 0);
                ps.setDouble(4, 0);
                ps.setDouble(5, 1);
                ps.setInt(6, 1);
                ps.setInt(7, 0);
                ps.setInt(8, 1);
                ps.setInt(9, 0);
                ps.setInt(10, 0);
                ps.setInt(11, 0);
                ps.setInt(12, 0);
                ps.setInt(13, 0);
                ps.setDouble(14, 0);
                ps.setDouble(15, 0);
                ps.setDouble(16, 0);
                ps.setString(17, uuid.toString());

                ps.executeUpdate();
                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to create data for " + name + ": " + e.getMessage());
        }
    }

    @NotNull
    public PlayerStats loadPlayerData(@NotNull UUID uuid, String tableName, String prefix) {
        try (Connection conn = databaseLib.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM " + tableName + " WHERE playerUUID = ?")
        ) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerStats(
                            rs.getString("playerName"),
                            rs.getDouble("playerEXP"),
                            rs.getDouble("playerBXP"),
                            rs.getDouble("playerXPM"),
                            rs.getInt("playerLevel"),
                            rs.getInt("playerLuck"),
                            rs.getInt("traitPoints"),
                            rs.getInt("talentPoints"),
                            rs.getInt("wisdomTrait"),
                            rs.getInt("charismaTrait"),
                            rs.getInt("karmaTrait"),
                            rs.getInt("dexterityTrait"),
                            rs.getInt("data1"),
                            rs.getInt("data2"),
                            rs.getInt("data3")
                    );
                }
            }

            // If we get here, the player doesn't exist in the database
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String playerName = player.getName() != null ? player.getName() : "Unknown";
            createNewPlayerData(uuid, playerName, tableName, prefix);

            // Try loading again
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new PlayerStats(
                            rs.getString("playerName"),
                            rs.getDouble("playerEXP"),
                            rs.getDouble("playerBXP"),
                            rs.getDouble("playerXPM"),
                            rs.getInt("playerLevel"),
                            rs.getInt("playerLuck"),
                            rs.getInt("traitPoints"),
                            rs.getInt("talentPoints"),
                            rs.getInt("wisdomTrait"),
                            rs.getInt("charismaTrait"),
                            rs.getInt("karmaTrait"),
                            rs.getInt("dexterityTrait"),
                            rs.getInt("data1"),
                            rs.getInt("data2"),
                            rs.getInt("data3")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to load data for player " + uuid + ": " + e.getMessage());
        }

        // If everything fails, return default stats
        return new PlayerStats(
                "Unknown",
                0.0,
                0.0,
                1.0,
                1,
                0,
                1,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }

    public void levelingEffects(@NotNull Player player, int newLevel, int previousLevel, int traitPoints) {
        EffectsUtil.sendTitleMessage(player,
                "<gradient:#ebd197:#a2790d>Archaeology</gradient>",
                "<#00FFA2>Level " + previousLevel + " ➛ " + newLevel);
        EffectsUtil.sendPlayerMessage(player,"<gradient:#FFE6A3:#FFD06E>You have </gradient><#00FFA2>" +
                traitPoints + " trait points <gradient:#FFE6A3:#FFD06E>that you can spend on [/varch trait].</gradient>");
        if (newLevel == 99 || newLevel == MAX_LEVEL) {
            EffectsUtil.spawnFireworks(plugin, player, 12, 3);
            EffectsUtil.playSound(player, "minecraft:cozyvanilla.all.master_levelup", Sound.Source.PLAYER, 1.0f, 1.0f);
        } else {
            EffectsUtil.spawnFireworks(plugin, player, 5, 5);
            EffectsUtil.playSound(player, "minecraft:cozyvanilla.archaeology.default_levelup", Sound.Source.PLAYER, 1.0f, 1.0f);
        }
    }

    public double getNewEXP(@NotNull EnumsLib.UpdateType type, double currentEXP, double value) {
        if (value <= 0) return currentEXP;

        value = DigitUtils.roundToPrecision(value, 2);
        switch (type) {
            case ADD -> { return Math.min(MAX_EXP, currentEXP + value); }
            case SUBTRACT -> { return Math.max(0, currentEXP - value); }
            case SET -> { return Math.max(0, Math.min(value, MAX_EXP)); }
            default -> { return currentEXP; }
        }
    }

    public int getNewLevel(@NotNull EnumsLib.UpdateType type, int currentLevel, int value) {
        if (value <= 0) return currentLevel;

        switch (type) {
            case ADD -> { return Math.min(MAX_LEVEL, currentLevel + value); }
            case SUBTRACT -> { return Math.max(MIN_LEVEL, currentLevel - value); }
            case SET -> { return Math.min(value, MAX_LEVEL); }
            default -> { return currentLevel; }
        }
    }

    public double getNewXPM(@NotNull EnumsLib.UpdateType type, double currentXPM, double value) {
        if (value < 0) return currentXPM;

        value = DigitUtils.roundToPrecision(value, 2);
        switch (type) {
            case ADD -> { return currentXPM + value; }
            case SUBTRACT -> { return Math.max(0, currentXPM - value); }
            case SET -> { return Math.max(0, value); }
            default -> { return currentXPM; }
        }
    }

    public double getNewBXP(@NotNull EnumsLib.UpdateType type, double currentBXP, double value) {
        if (value <= 0) return currentBXP;

        value = DigitUtils.roundToPrecision(value, 2);
        switch (type) {
            case ADD -> { return currentBXP + value; }
            case SUBTRACT -> { return Math.max(0, currentBXP - value); }
            case SET -> { return Math.max(0, value); }
            default -> { return currentBXP; }
        }
    }

    public int getNewTraitPoints(@NotNull EnumsLib.UpdateType type, int currentTP, int value) {
        if (value <= 0) return currentTP;

        switch (type) {
            case ADD -> { return currentTP + value; }
            case SUBTRACT -> { return Math.max(0, currentTP - value); }
            case SET -> { return value; }
            default -> { return currentTP; }
        }
    }

    public int getNewTalentPoints(@NotNull EnumsLib.UpdateType type, int currentTP, int value) {
        if (value <= 0) return currentTP;

        switch (type) {
            case ADD -> { return currentTP + value; }
            case SUBTRACT -> { return Math.max(0, currentTP - value); }
            case SET -> { return value; }
            default -> { return currentTP; }
        }
    }

    public double getNewData1(@NotNull EnumsLib.UpdateType type, double currentData, double value) {
        if (value <= 0) return currentData;

        switch (type) {
            case ADD -> { return currentData + value; }
            case SUBTRACT -> { return Math.max(0, currentData - value); }
            case SET -> { return value; }
            default -> { return currentData; }
        }
    }

    public double getNewData2(@NotNull EnumsLib.UpdateType type, double currentData, int value) {
        if (value <= 0) return currentData;

        switch (type) {
            case ADD -> { return currentData + value; }
            case SUBTRACT -> { return Math.max(0, currentData - value); }
            case SET -> { return value; }
            default -> { return currentData; }
        }
    }

    public double getNewData3(@NotNull EnumsLib.UpdateType type, double currentData, int value) {
        if (value <= 0) return currentData;

        switch (type) {
            case ADD -> { return currentData + value; }
            case SUBTRACT -> { return Math.max(0, currentData - value); }
            case SET -> { return value; }
            default -> { return currentData; }
        }
    }

    public int getNewLuck(@NotNull EnumsLib.UpdateType type, int luck, int value) {
        if (value <= 0) return luck;

        switch (type) {
            case ADD -> { return luck + value; }
            case SUBTRACT -> { return Math.max(0, luck - value); }
            case SET -> { return value; }
            default -> { return luck; }
        }
    }
}
