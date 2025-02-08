package asia.virtualmc.vLibrary.configs;

import asia.virtualmc.vLibrary.utils.ConsoleMessageUtil;
import asia.virtualmc.vLibrary.var.GlobalVariables;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class EXPTableConfig {
    private final Plugin plugin;
    public static List<Integer> ARCH_EXP_TABLE;
    public static List<Integer> FISH_EXP_TABLE;
    public static List<Integer> MINE_EXP_TABLE;
    public static List<Integer> INV_EXP_TABLE;
    public static List<Integer> DEF_EXP_TABLE;

    public EXPTableConfig(@NotNull ConfigManager configManager) {
        this.plugin = configManager.getVlib();

        DEF_EXP_TABLE = loadDefaultTable();
        ARCH_EXP_TABLE = loadEXPTable(GlobalVariables.archPlugin, "vArchaeology");
        FISH_EXP_TABLE = loadEXPTable(GlobalVariables.fishPlugin, "vFishing");
        MINE_EXP_TABLE = loadEXPTable(GlobalVariables.minePlugin, "vMining");
        INV_EXP_TABLE = loadEXPTable(GlobalVariables.invPlugin, "vInvention");
    }

    private List<Integer> loadDefaultTable() {
        File defFile = new File(plugin.getDataFolder(), "default-experience.yml");

        if (!defFile.exists()) {
            ConsoleMessageUtil.printSevere("[vLibrary] Missing default-experience.yml. Creating a new one with default values.");
            plugin.saveResource("default-experience.yml", false);
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

    private List<Integer> loadEXPTable(Plugin plugin, String pluginName) {
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
