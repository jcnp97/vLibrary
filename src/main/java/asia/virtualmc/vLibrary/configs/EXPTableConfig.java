package asia.virtualmc.vLibrary.configs;

import asia.virtualmc.vLibrary.VLibrary;
import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class EXPTableConfig {
    private final VLibrary vlib;
    public static List<Integer> DEF_EXP_TABLE;

    public EXPTableConfig(@NotNull ConfigManager configManager) {
        this.vlib = configManager.getVlib();
        DEF_EXP_TABLE = loadDefaultTable();
    }

    private List<Integer> loadDefaultTable() {
        File defFile = new File(vlib.getDataFolder(), "default-experience.yml");

        if (!defFile.exists()) {
            ConsoleMessageUtil.printSevere("[vLibrary] Missing default-experience.yml. Creating a new one with default values.");
            vlib.saveResource("default-experience.yml", false);
        }

        FileConfiguration expConfig = YamlConfiguration.loadConfiguration(defFile);

        if (expConfig.getKeys(false).isEmpty()) {
            ConsoleMessageUtil.printSevere("[vLibrary] default-experience.yml is empty or invalid.");
            return Collections.emptyList();
        }

        List<Integer> tempList = new ArrayList<>();
        int previousExp = -1;

        for (String key : expConfig.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                String expString = expConfig.getString(key, "0").replace(",", "");
                int exp = Integer.parseInt(expString);

                if (previousExp >= 0 && exp <= previousExp) {
                    throw new IllegalStateException("[vLibrary] Invalid progression: Level " + level +
                            " has lower or equal EXP than previous level");
                }

                tempList.add(exp);
                previousExp = exp;
            } catch (NumberFormatException | IllegalStateException e) {
                ConsoleMessageUtil.printSevere("[vLibrary] Failed to load default exp table: " + e.getMessage());
                return Collections.emptyList();
            }
        }

        return Collections.unmodifiableList(tempList);
    }

    public static List<Integer> loadEXPTable(Plugin plugin, String pluginName) {
        if (plugin == null) {
            ConsoleMessageUtil.printSevere("[vLibrary] " + pluginName + " not found! Using default exp table.");
            return DEF_EXP_TABLE;
        }

        File expFile = new File(plugin.getDataFolder(), "experience-table.yml");

        if (!expFile.exists()) {
            try {
                plugin.saveResource("experience-table.yml", false);
            } catch (Exception e) {
                ConsoleMessageUtil.printSevere("[vLibrary] Failed to save " + pluginName + " exp table: " + e.getMessage());
                return DEF_EXP_TABLE;
            }
        }

        FileConfiguration expConfig = YamlConfiguration.loadConfiguration(expFile);

        if (expConfig.getKeys(false).isEmpty()) {
            ConsoleMessageUtil.printSevere("[vLibrary] " + pluginName + " experience-table.yml is empty or invalid. Using default exp table.");
            return DEF_EXP_TABLE;
        }

        List<Integer> tempList = new ArrayList<>();
        int previousExp = -1;

        for (String key : expConfig.getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                String expString = expConfig.getString(key, "0").replace(",", "");
                int exp = Integer.parseInt(expString);

                if (previousExp >= 0 && exp <= previousExp) {
                    throw new IllegalStateException("[vLibrary] Invalid progression: Level " + level +
                            " has lower or equal EXP than previous level");
                }

                tempList.add(exp);
                previousExp = exp;
            } catch (NumberFormatException | IllegalStateException e) {
                ConsoleMessageUtil.printSevere("[vLibrary] Failed to load " + pluginName + " exp table: " + e.getMessage());
                ConsoleMessageUtil.printSevere("[vLibrary] Using the default exp table.");
                return DEF_EXP_TABLE;
            }
        }

        return Collections.unmodifiableList(tempList);
    }
}
