package asia.virtualmc.vLibrary.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DropsConfig {

    private static final String CONFIG_PATH = "items/drops.yml";
    private static final String BASE_PATH = "dropSettings";

    /**
     * Reads EXP values from the drops configuration file.
     *
     * @param plugin The plugin instance
     * @return List of EXP values for each rarity
     */
    public static List<Integer> readDropEXP(@NotNull Plugin plugin) {
        return readIntegerList(plugin, BASE_PATH + ".exp");
    }

    /**
     * Reads drop weights from the drops configuration file.
     *
     * @param plugin The plugin instance
     * @return List of weight values for each rarity
     */
    public static List<Integer> readDropWeights(@NotNull Plugin plugin) {
        return readIntegerList(plugin, BASE_PATH + ".weight");
    }

    /**
     * Reads sell prices from the drops configuration file.
     *
     * @param plugin The plugin instance
     * @return List of sell price values for each rarity
     */
    public static List<Integer> readDropSellPrice(@NotNull Plugin plugin) {
        return readIntegerList(plugin, BASE_PATH + ".sell-price");
    }

    /**
     * Reads starting max weight values from the drops configuration file.
     *
     * @param plugin The plugin instance
     * @return List of starting max weight values for each rarity
     */
    public static List<Integer> readStartingMaxWeight(@NotNull Plugin plugin) {
        return readIntegerList(plugin, BASE_PATH + ".starting-max-weight");
    }

    /**
     * Reads quality multiplier values from the drops configuration file.
     *
     * @param plugin The plugin instance
     * @return List of quality multiplier values
     */
    public static List<Double> readQualityMultiplier(@NotNull Plugin plugin) {
        return readDoubleList(plugin, BASE_PATH + ".quality-multiplier");
    }

    /**
     * Generic method to read a list of integer values from a configuration section.
     *
     * @param plugin The plugin instance
     * @param path The configuration path to read from
     * @return List of integer values in the configuration section
     */
    private static List<Integer> readIntegerList(@NotNull Plugin plugin, String path) {
        List<Integer> values = new ArrayList<>();
        FileConfiguration config = getConfiguration(plugin);

        if (config == null) {
            return values;
        }

        try {
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section != null) {
                Set<String> keys = section.getKeys(false);
                for (String key : keys) {
                    values.add(section.getInt(key, 0));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("There was an error when reading values from " + path + ": " + e.getMessage());
        }

        return values;
    }

    /**
     * Generic method to read a list of double values from a configuration section.
     *
     * @param plugin The plugin instance
     * @param path The configuration path to read from
     * @return List of double values in the configuration section
     */
    private static List<Double> readDoubleList(@NotNull Plugin plugin, String path) {
        List<Double> values = new ArrayList<>();
        FileConfiguration config = getConfiguration(plugin);

        if (config == null) {
            return values;
        }

        try {
            ConfigurationSection section = config.getConfigurationSection(path);
            if (section != null) {
                Set<String> keys = section.getKeys(false);
                for (String key : keys) {
                    values.add(section.getDouble(key, 0.0));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("There was an error when reading values from " + path + ": " + e.getMessage());
        }

        return values;
    }

    /**
     * Retrieves the configuration file, creating it if it doesn't exist.
     *
     * @param plugin The plugin instance
     * @return The loaded configuration or null if an error occurred
     */
    private static FileConfiguration getConfiguration(@NotNull Plugin plugin) {
        File configFile = new File(plugin.getDataFolder(), CONFIG_PATH);
        if (!configFile.exists()) {
            try {
                plugin.saveResource(CONFIG_PATH, false);
            } catch (Exception e) {
                plugin.getLogger().severe("Couldn't save/load " + CONFIG_PATH + ": " + e.getMessage());
                return null;
            }
        }
        return YamlConfiguration.loadConfiguration(configFile);
    }
}