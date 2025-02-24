package asia.virtualmc.vLibrary.configs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MaterialBlockConfig {

    public static Map<Material, Integer> loadArchBlocks(@NotNull Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection blocksSection = config.getConfigurationSection("settings.blocksList");
        Map<Material, Integer> blocksList = new HashMap<>();

        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    int expValue = blocksSection.getInt(key);
                    blocksList.put(material, expValue);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in config: " + key);
                }
            }
        }
        return blocksList;
    }
}
