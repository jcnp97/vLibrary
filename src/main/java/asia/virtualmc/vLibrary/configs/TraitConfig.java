package asia.virtualmc.vLibrary.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class TraitConfig {

    public static Map<String, Double> getTraitEffects(@NotNull Plugin plugin) {
        Map<String, Double> traitEffects = new LinkedHashMap<>();
        File traitsFile = new File(plugin.getDataFolder(), "traits.yml");

        if (!traitsFile.exists()) {
            try {
                plugin.saveResource("traits.yml", false);
            } catch (Exception e) {
                e.printStackTrace();
                return traitEffects;
            }
        }

        FileConfiguration trait = YamlConfiguration.loadConfiguration(traitsFile);
        try {
            ConfigurationSection traitListSection = trait.getConfigurationSection("traitList");
            if (traitListSection != null) {
                // Loop through all traits (wisdom, charisma, karma, dexterity)
                for (String traitName : traitListSection.getKeys(false)) {
                    ConfigurationSection effectsSection = traitListSection.getConfigurationSection(traitName + ".effects");
                    if (effectsSection != null) {
                        // Loop through all effects for this trait
                        for (String effectName : effectsSection.getKeys(false)) {
                            String key = traitName + "_" + effectName;

                            // Check for duplicate keys (should not happen with proper naming, but just in case)
                            if (traitEffects.containsKey(key)) {
                                plugin.getLogger().warning("Duplicate trait effect key found: " + key + ". Skipping...");
                                continue;
                            }

                            double value = effectsSection.getDouble(effectName, 0.0);
                            traitEffects.put(key, value);
                        }
                    } else {
                        plugin.getLogger().warning("No effects section found for trait: " + traitName);
                    }
                }
            } else {
                plugin.getLogger().warning("No traitList section found in traits.yml");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Couldn't load trait values: " + e.getMessage());
        }

        return traitEffects;
    }
}
