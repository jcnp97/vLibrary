package asia.virtualmc.vLibrary.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class RankConfig {

    public static record rankInfo(int pointsRequired, String rankName) {}

    public static Map<Integer, rankInfo> readRanksFromFile(Plugin plugin) {
        Map<Integer, rankInfo> rankTable = new LinkedHashMap<>();
        File rankTableFile = new File(plugin.getDataFolder(), "ranks.yml");

        if (!rankTableFile.exists()) {
            try {
                plugin.saveResource("ranks.yml", false);
            } catch (Exception e) {
                plugin.getLogger().severe("Rank table not found.");
                return rankTable;
            }
        }

        FileConfiguration ranksConfig = YamlConfiguration.loadConfiguration(rankTableFile);
        ConfigurationSection ranksSection = ranksConfig.getConfigurationSection("ranksList");

        if (ranksSection == null) {
            plugin.getLogger().severe("No 'ranksList' section found in ranks.yml");
            return rankTable;
        }

        int prevPoints = -1;

        for (String key : ranksSection.getKeys(false)) {
            try {
                int rankLevel = Integer.parseInt(key);
                int points = ranksSection.getInt(key + ".points");
                String rankName = ranksSection.getString(key + ".rankName");

                if (prevPoints >= 0 && points <= prevPoints) {
                    plugin.getLogger().severe("Invalid progression: Rank " + rankLevel +
                            " has lower or equal EXP than previous level");
                    continue; // Skip this invalid entry but continue processing others
                }

                rankTable.put(rankLevel, new rankInfo(points, rankName));
                prevPoints = points;
            } catch (NumberFormatException e) {
                plugin.getLogger().severe("Invalid number format in rank table at level " + key);
            }
        }

        return rankTable;
    }

    public static Map<String, Double> readRankSettingsFromFile(Plugin plugin) {
        Map<String, Double> settings = new HashMap<>();
        File configFile = new File(plugin.getDataFolder(), "ranks.yml");

        if (!configFile.exists()) {
            try {
                plugin.saveResource("ranks.yml", false);
                plugin.getLogger().info("Created default ranks.yml file");
            } catch (Exception e) {
                plugin.getLogger().severe("Could not create ranks.yml file: " + e.getMessage());
                return settings;
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection globalSection = config.getConfigurationSection("settings");

        if (globalSection == null) {
            plugin.getLogger().warning("No 'settings' section found in ranks.yml");
            return settings;
        }

        for (String key : globalSection.getKeys(false)) {
            try {
                double value = globalSection.getDouble(key);
                settings.put(key, value);
                plugin.getLogger().info("Loaded global setting: " + key + " = " + value);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load global setting '" + key + "': " + e.getMessage());
            }
        }

        // Validate required settings are present
        String[] requiredSettings = {"fish-caught-xp", "material-get-xp", "fish-deliveries", "tax-reduction"};
        for (String setting : requiredSettings) {
            if (!settings.containsKey(setting)) {
                plugin.getLogger().warning("Missing required global setting: " + setting);
            }
        }

        return settings;
    }
}
