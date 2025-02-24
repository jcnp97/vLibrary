package asia.virtualmc.vLibrary.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class TalentTreeConfig {

    public static List<String> loadTalentNames(@NotNull Plugin plugin) {
        File talentFile = new File(plugin.getDataFolder(), "talent-trees.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(talentFile);
        List<String> talentNames = new ArrayList<>();

        if (config.isConfigurationSection("talentList")) {
            for (String key : config.getConfigurationSection("talentList").getKeys(false)) {
                String name = config.getString("talentList." + key + ".name");
                if (name != null) {
                    talentNames.add(name);
                }
            }
        }
        return talentNames;
    }
}
