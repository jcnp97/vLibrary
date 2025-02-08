package asia.virtualmc.vLibrary.storage;

import asia.virtualmc.vLibrary.enums.EnumsLib;
import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import asia.virtualmc.vLibrary.utils.DigitUtils;
import asia.virtualmc.vLibrary.utils.EffectsUtil;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.bukkit.Bukkit.*;

public class PlayerDataLib {
    private final Plugin plugin;
    private final DatabaseLib databaseLib;
    private final EffectsUtil effectsUtil;
    private static final int MAX_EXP = 2_147_483_647;
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 120;

    public PlayerDataLib(@NotNull StorageManagerLib storageManager) {
        this.plugin = storageManager.getMain();
        this.databaseLib = storageManager.getDatabaseLib();
        this.effectsUtil = storageManager.getEffectsUtil();
    }

    public static class PlayerStats {
        String name;
        double exp;
        double bxp;
        double xpm;
        int level;
        int luck;
        int traitPoints;
        int talentPoints;
        int wisdomTrait;
        int charismaTrait;
        int karmaTrait;
        int dexterityTrait;
        int rank;

        PlayerStats(String name, double exp, double bxp, double xpm, int level, int luck,
                    int traitPoints, int talentPoints, int wisdomTrait, int charismaTrait,
                    int karmaTrait, int dexterityTrait, int rank
        ) {
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
            this.rank = rank;
        }
    }

    public void createTable(String tableName, String prefix) {
        if (databaseLib == null) {
            Bukkit.getLogger().severe(prefix + "Database is not initialized. Cannot create table.");
            return;
        }

        try (Connection conn = databaseLib.getDataSource().getConnection()) {
            conn.createStatement().execute("CREATE TABLE IF NOT EXISTS " + tableName +
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
                    "numericalRank INT DEFAULT 0)"
            );
        } catch (SQLException e) {
            Bukkit.getLogger().severe(prefix + "Failed to create player data table: " + e.getMessage());
        }
    }

    public void storePlayerData(
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
            int rank,
            String tableName,
            String prefix
    ) {
        try (Connection conn = databaseLib.getDataSource().getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE " + tableName + " SET " +
                            "playerName = ?, " +
                            "playerEXP = ?, " +
                            "playerBXP = ?, " +
                            "playerXPM = ?, " +
                            "playerLevel = ?, " +
                            "playerLuck = ?, " +
                            "traitPoints = ?, " +
                            "talentPoints = ?, " +
                            "wisdomTrait = ?, " +
                            "charismaTrait = ?, " +
                            "karmaTrait = ?, " +
                            "dexterityTrait = ?, " +
                            "numericalRank = ? " +
                            "WHERE playerUUID = ?"
            );
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
            ps.setInt(13, rank);
            ps.setString(14, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to save " + name + " data on database: " + e.getMessage());
        }
    }

    public void createNewPlayerData(@NotNull UUID uuid, String name, String tableName, String prefix) {
        Connection conn = null;
        try {
            conn = databaseLib.getDataSource().getConnection();
            conn.setAutoCommit(false);
            try {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO " + tableName + " (" +
                                "playerUUID, " +
                                "playerName, " +
                                "playerEXP, " +
                                "playerBXP, " +
                                "playerXPM, " +
                                "playerLevel, " +
                                "playerLuck, " +
                                "traitPoints, " +
                                "talentPoints, " +
                                "wisdomTrait, " +
                                "charismaTrait, " +
                                "karmaTrait, " +
                                "dexterityTrait, " +
                                "numericalRank " +
                                ") VALUES (" +
                                "?, ?, ?, ?, ?," +
                                "?, ?, ?, ?, ?," +
                                "?, ?, ?, ?" +
                                ")"
                );
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
                ps.setInt(14, 0);
                ps.executeUpdate();

                conn.commit();
                //ConsoleMessageUtil.print(prefix + "Successfully created new primary data for " + name);

            } catch (SQLException e) {
                conn.rollback();
                plugin.getLogger().severe(prefix + "Failed to create primary data for " + name + ". Error: " + e.getMessage());
                throw e;
            }

        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Database error while creating primary data: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    plugin.getLogger().severe(prefix + "Error closing database connection: " + e.getMessage());
                }
            }
        }
    }

    @Nullable
    public ResultSet getPlayerData(@NotNull UUID uuid, String tableName, String prefix) {
        OfflinePlayer player = getPlayer(uuid);
        try {
            Connection conn = databaseLib.getDataSource().getConnection();
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM " + tableName + " WHERE playerUUID = ?"
            );
            ps.setString(1, uuid.toString());
            return ps.executeQuery();
        } catch (SQLException e) {
            assert player != null;
            plugin.getLogger().severe(prefix + "Cannot retrieve " + player.getName() + " data: " + e.getMessage());
            return null;
        }
    }

    public PlayerStats loadPlayerData(@NotNull UUID uuid, String tableName, String prefix) {
        try (ResultSet rs = getPlayerData(uuid, tableName, prefix)) {
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
                        rs.getInt("numericalRank")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe(prefix + "Failed to load data for player " + getPlayer(uuid) + ": " + e.getMessage());
        }
        return null;
    }

    public void levelingEffects(@NotNull Player player, int newLevel, int previousLevel, int traitPoints) {
        EffectsUtil.sendTitleMessage(player,
                "<gradient:#ebd197:#a2790d>Archaeology</gradient>",
                "<#00FFA2>Level " + previousLevel + " ➛ " + newLevel);
        EffectsUtil.sendPlayerMessage(player,"<gradient:#FFE6A3:#FFD06E>You have </gradient><#00FFA2>" +
                traitPoints + " trait points <gradient:#FFE6A3:#FFD06E>that you can spend on [/varch trait].</gradient>");
        if (newLevel == 99 || newLevel == MAX_LEVEL) {
            effectsUtil.spawnFireworks(player, 12, 3);
            effectsUtil.playSound(player, "minecraft:cozyvanilla.all.master_levelup", Sound.Source.PLAYER, 1.0f, 1.0f);
        } else {
            effectsUtil.spawnFireworks(player, 5, 5);
            effectsUtil.playSound(player, "minecraft:cozyvanilla.archaeology.default_levelup", Sound.Source.PLAYER, 1.0f, 1.0f);
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

    public int getNewNumericalRank(@NotNull EnumsLib.UpdateType type, int currentNR, int value) {
        if (value <= 0) return currentNR;

        switch (type) {
            case ADD -> { return currentNR + value; }
            case SUBTRACT -> { return Math.max(0, currentNR - value); }
            case SET -> { return value; }
            default -> { return currentNR; }
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
