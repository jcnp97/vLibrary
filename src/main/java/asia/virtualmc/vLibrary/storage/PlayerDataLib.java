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
    private final LibraryData libraryData;
    private static final int MAX_EXP = 2_147_483_647;
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 120;

    public PlayerDataLib(@NotNull StorageManagerLib storageManager) {
        this.plugin = storageManager.getMain();
        this.libraryData = storageManager.getLibraryData();
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

        public PlayerStats(String name, double exp, double bxp, double xpm, int level, int luck,
                           int traitPoints, int talentPoints, int wisdomTrait, int charismaTrait,
                           int karmaTrait, int dexterityTrait) {
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
        }
    }

    /**
     * Creates the player data table without storing the player's UUID.
     * The table now uses playerID (sourced from vlib_players) as the PRIMARY KEY.
     */
    public void createTable(@NotNull String tablePrefix, String prefix) {
        String sql = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "_playerData (" +
                "playerID INT NOT NULL PRIMARY KEY, " +
                "playerName VARCHAR(16) NOT NULL, " +
                "playerEXP DECIMAL(13,2) DEFAULT 0.00, " +
                "playerBXP DECIMAL(13,2) DEFAULT 0.00, " +
                "playerXPM DECIMAL(4,2) DEFAULT 1.00, " +
                "playerLevel TINYINT DEFAULT 1, " +
                "playerLuck TINYINT DEFAULT 0, " +
                "traitPoints INT DEFAULT 1, " +
                "talentPoints INT DEFAULT 0, " +
                "wisdomTrait INT DEFAULT 0, " +
                "charismaTrait INT DEFAULT 0, " +
                "karmaTrait INT DEFAULT 0, " +
                "dexterityTrait INT DEFAULT 0, " +
                "lastUpdated TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Connection conn = libraryData.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create player data table: " + e.getMessage());
        }
    }

    /**
     * Saves the player data using the external getPlayerID(UUID) method to reference the correct playerID.
     */
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
            String tablePrefix,
            String prefix
    ) {
        Integer playerID = libraryData.getPlayerID(uuid);
        if (playerID == null) {
            plugin.getLogger().severe(prefix + "getPlayerID returned NULL for UUID: " + uuid);
            return;
        }

        String updateQuery = "UPDATE " + tablePrefix + "_playerData SET " +
                "playerName = ?, playerEXP = ?, playerBXP = ?, " +
                "playerXPM = ?, playerLevel = ?, playerLuck = ?, " +
                "traitPoints = ?, talentPoints = ?, wisdomTrait = ?, " +
                "charismaTrait = ?, karmaTrait = ?, dexterityTrait = ?, " +
                "lastUpdated = CURRENT_TIMESTAMP " +
                "WHERE playerID = ?";

        try (Connection conn = libraryData.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {

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
            ps.setInt(13, playerID);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                // If no rows were updated, the player's data row doesn't exist yet.
                createNewPlayerData(uuid, name, tablePrefix, prefix);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to save " + name + " data on database: " + e.getMessage());
        }
    }

    /**
     * Saves all players' data in batch using playerID as the key.
     */
    public void saveAllData(
            @NotNull Map<UUID, PlayerStats> playerDataMap,
            String tablePrefix,
            String prefix
    ) {
        if (playerDataMap.isEmpty()) {
            return;
        }

        String updateQuery = "UPDATE " + tablePrefix + "_playerData SET " +
                "playerName = ?, playerEXP = ?, playerBXP = ?, " +
                "playerXPM = ?, playerLevel = ?, playerLuck = ?, " +
                "traitPoints = ?, talentPoints = ?, wisdomTrait = ?, " +
                "charismaTrait = ?, karmaTrait = ?, dexterityTrait = ?, " +
                "lastUpdated = CURRENT_TIMESTAMP " +
                "WHERE playerID = ?";

        try (Connection conn = libraryData.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {

            conn.setAutoCommit(false);
            int batchSize = 0;

            for (Map.Entry<UUID, PlayerStats> entry : playerDataMap.entrySet()) {
                UUID uuid = entry.getKey();
                PlayerStats stats = entry.getValue();
                Integer playerID = libraryData.getPlayerID(uuid);
                if (playerID == null) {
                    plugin.getLogger().severe(prefix + "getPlayerID returned NULL for UUID: " + uuid);
                    continue;
                }

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
                ps.setInt(13, playerID);

                ps.addBatch();
                batchSize++;

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

    /**
     * Inserts a new player data record using the playerID provided by getPlayerID(UUID).
     */
    public void createNewPlayerData(@NotNull UUID uuid, String name, String tablePrefix, String prefix) {
        Integer playerID = libraryData.getPlayerID(uuid);
        if (playerID == null) {
            plugin.getLogger().severe(prefix + "getPlayerID returned NULL for UUID: " + uuid);
            return;
        }

        String insertQuery =
                "INSERT INTO " + tablePrefix + "_playerData" +
                        " (playerID, playerName, playerEXP, playerBXP, playerXPM, " +
                        "playerLevel, playerLuck, traitPoints, talentPoints, wisdomTrait, " +
                        "charismaTrait, karmaTrait, dexterityTrait) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = libraryData.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertQuery)) {
                ps.setInt(1, playerID);
                ps.setString(2, name);
                ps.setDouble(3, 0.0);
                ps.setDouble(4, 0.0);
                ps.setDouble(5, 1.0);
                ps.setInt(6, 1);
                ps.setInt(7, 0);
                ps.setInt(8, 1);
                ps.setInt(9, 0);
                ps.setInt(10, 0);
                ps.setInt(11, 0);
                ps.setInt(12, 0);
                ps.setInt(13, 0);
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

    /**
     * Loads a player's data using the playerID obtained from getPlayerID(UUID).
     * If no record exists, a new entry is created.
     */
    @NotNull
    public PlayerStats loadPlayerData(@NotNull UUID uuid, String tablePrefix, String prefix) {
        Integer playerID = libraryData.getPlayerID(uuid);
        if (playerID == null) {
            plugin.getLogger().severe(prefix + "getPlayerID returned NULL for UUID: " + uuid);
            return new PlayerStats("Unknown", 0.0, 0.0, 1.0, 1, 0, 1, 0, 0, 0, 0, 0);
        }

        String selectQuery = "SELECT * FROM " + tablePrefix + "_playerData WHERE playerID = ?";
        try (Connection conn = libraryData.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(selectQuery)) {
            ps.setInt(1, playerID);

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
                            rs.getInt("dexterityTrait")
                    );
                }
            }

            // If no record is found, create one and try again.
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String playerName = (player.getName() != null) ? player.getName() : "Unknown";
            createNewPlayerData(uuid, playerName, tablePrefix, prefix);

            // Try loading again.
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
                            rs.getInt("dexterityTrait")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to load data for player " + uuid + ": " + e.getMessage());
        }

        return new PlayerStats("Unknown", 0.0, 0.0, 1.0, 1, 0, 1, 0, 0, 0, 0, 0);
    }

    // NON-RELATED METHODS TO DATABASE

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
