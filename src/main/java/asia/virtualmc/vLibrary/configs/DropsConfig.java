package asia.virtualmc.vLibrary.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class DropsConfig {

    public static int[] readDropEXPFile(@NotNull Plugin plugin, String prefix) {
        int[] dropEXP = {0, 0, 0, 0, 0, 0, 0};

        File dropsFile = new File(plugin.getDataFolder(), "items/drops.yml");
        if (!dropsFile.exists()) {
            try {
                plugin.saveResource("items/drops.yml", false);
            } catch (Exception e) {
                plugin.getLogger().severe(prefix + "Couldn't save/load drops.yml: " + e.getMessage());
                return dropEXP;
            }
        }

        FileConfiguration drops = YamlConfiguration.loadConfiguration(dropsFile);

        try {
            dropEXP[0] = drops.getInt("raritySettings.exp.common", 20);
            dropEXP[1] = drops.getInt("raritySettings.exp.uncommon", 35);
            dropEXP[2] = drops.getInt("raritySettings.exp.rare", 60);
            dropEXP[3] = drops.getInt("raritySettings.exp.unique", 80);
            dropEXP[4] = drops.getInt("raritySettings.exp.special", 150);
            dropEXP[5] = drops.getInt("raritySettings.exp.mythical", 300);
            dropEXP[6] = drops.getInt("raritySettings.exp.exotic", 1250);

        } catch (Exception e) {
            plugin.getLogger().severe(prefix + "There was an error when reading weights from drops.yml: " + e.getMessage());
            return dropEXP;
        }

        return dropEXP;
    }

    public static double[] readDropPriceFile(@NotNull Plugin plugin, String prefix) {
        double[] dropBasePrice = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

        File dropsFile = new File(plugin.getDataFolder(), "items/drops.yml");
        if (!dropsFile.exists()) {
            try {
                plugin.saveResource("items/drops.yml", false);
            } catch (Exception e) {
                plugin.getLogger().severe(prefix + "Couldn't save/load items/drops.yml: " + e.getMessage());
                return dropBasePrice;
            }
        }
        FileConfiguration drops = YamlConfiguration.loadConfiguration(dropsFile);

        try {
            dropBasePrice[0] = drops.getInt("raritySettings.sell-price.common", 0);
            dropBasePrice[1] = drops.getInt("raritySettings.sell-price.uncommon", 0);
            dropBasePrice[2] = drops.getInt("raritySettings.sell-price.rare", 0);
            dropBasePrice[3] = drops.getInt("raritySettings.sell-price.unique", 0);
            dropBasePrice[4] = drops.getInt("raritySettings.sell-price.special", 0);
            dropBasePrice[5] = drops.getInt("raritySettings.sell-price.mythical", 0);
            dropBasePrice[6] = drops.getInt("raritySettings.sell-price.exotic", 0);

        } catch (Exception e) {
            plugin.getLogger().severe(prefix + "There was an error when reading weights from items.yml: " + e.getMessage());
            return dropBasePrice;
        }

        return dropBasePrice;
    }

    public static int[] readDropWeightsFile(@NotNull Plugin plugin, String prefix) {
        int[] dropWeights = {0, 0, 0, 0, 0, 0, 0};

        File dropsFile = new File(plugin.getDataFolder(), "items/drops.yml");
        if (!dropsFile.exists()) {
            try {
                plugin.saveResource("items/drops.yml", false);
            } catch (Exception e) {
                plugin.getLogger().severe(prefix + "Couldn't save/load drops.yml: " + e.getMessage());
                return dropWeights;
            }
        }

        FileConfiguration drops = YamlConfiguration.loadConfiguration(dropsFile);

        try {
            dropWeights[0] = drops.getInt("raritySettings.weight.common", 55);
            dropWeights[1] = drops.getInt("raritySettings.weight.uncommon", 35);
            dropWeights[2] = drops.getInt("raritySettings.weight.rare", 25);
            dropWeights[3] = drops.getInt("raritySettings.weight.unique", 15);
            dropWeights[4] = drops.getInt("raritySettings.weight.special", 8);
            dropWeights[5] = drops.getInt("raritySettings.weight.mythical", 4);
            dropWeights[6] = drops.getInt("raritySettings.weight.exotic", 1);

        } catch (Exception e) {
            plugin.getLogger().severe(prefix + "There was an error when reading weights from drops.yml: " + e.getMessage());
            return dropWeights;
        }

        return dropWeights;
    }
}
