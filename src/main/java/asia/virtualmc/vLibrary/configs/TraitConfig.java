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

//    public static double[] getWisdomEffects(@NotNull Plugin plugin, String prefix) {
//        double[] wisdomEffects = {0.0, 0.0, 0.0, 0.0};
//        File dropsFile = new File(plugin.getDataFolder(), "traits.yml");
//        if (!dropsFile.exists()) {
//            try {
//                plugin.saveResource("traits.yml", false);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return wisdomEffects;
//            }
//        }
//
//        FileConfiguration trait = YamlConfiguration.loadConfiguration(dropsFile);
//        try {
//            wisdomEffects[0] = trait.getDouble("traitList.wisdom.effects.block-break", 0.0);
//            wisdomEffects[1] = trait.getDouble("traitList.wisdom.effects.receive-material", 0.0);
//            wisdomEffects[2] = trait.getDouble("traitList.wisdom.effects.artefact-restoration", 0.0);
//            wisdomEffects[3] = trait.getDouble("traitList.wisdom.effects.max-trait-bonus", 0.0);
//
//        } catch (Exception e) {
//            plugin.getLogger().severe(prefix + "Couldn't load wisdom trait values :" + e.getMessage());
//        }
//        return wisdomEffects;
//    }

//    public static double[] getCharismaEffects(@NotNull Plugin plugin, String prefix) {
//        double[] charismaEffects = {0.0, 0.0, 0.0, 0.0};
//        File dropsFile = new File(plugin.getDataFolder(), "traits.yml");
//        if (!dropsFile.exists()) {
//            try {
//                plugin.saveResource("traits.yml", false);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return charismaEffects;
//            }
//        }
//
//        FileConfiguration trait = YamlConfiguration.loadConfiguration(dropsFile);
//        try {
//            charismaEffects[0] = trait.getDouble("traitList.charisma.effects.archaeology-drops", 0.0);
//            charismaEffects[1] = trait.getDouble("traitList.charisma.effects.artefacts", 0.0);
//            charismaEffects[2] = trait.getDouble("traitList.charisma.effects.aptitude-gain", 0.0);
//            charismaEffects[3] = trait.getDouble("traitList.charisma.effects.max-trait-bonus", 0.0);
//        } catch (Exception e) {
//            plugin.getLogger().severe(prefix + "Couldn't load charisma trait values :" + e.getMessage());
//        }
//        return charismaEffects;
//    }
//
//    public static double[] getKarmaEffects(@NotNull Plugin plugin, String prefix) {
//        double[] karmaEffects = {0.0, 0.0, 0.0, 0.0};
//        File dropsFile = new File(plugin.getDataFolder(), "traits.yml");
//        if (!dropsFile.exists()) {
//            try {
//                plugin.saveResource("traits.yml", false);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return karmaEffects;
//            }
//        }
//        FileConfiguration trait = YamlConfiguration.loadConfiguration(dropsFile);
//        try {
//            karmaEffects[0] = trait.getDouble("traitList.karma.effects.gathering-rate", 0.0);
//            karmaEffects[1] = trait.getDouble("traitList.karma.effects.extra-roll", 0.0);
//            karmaEffects[2] = trait.getDouble("traitList.karma.effects.next-tier-roll", 0.0);
//            karmaEffects[3] = trait.getDouble("traitList.karma.effects.max-trait-bonus", 0.0);
//        } catch (Exception e) {
//            plugin.getLogger().severe(prefix + "Couldn't load karma trait values :" + e.getMessage());
//        }
//        return karmaEffects;
//    }
//
//    public static double[] getDexterityEffects(@NotNull Plugin plugin, String prefix) {
//        double[] dexterityEffects = {0.0, 0.0, 0.0, 0.0};
//        File dropsFile = new File(plugin.getDataFolder(), "traits.yml");
//        if (!dropsFile.exists()) {
//            try {
//                plugin.saveResource("traits.yml", false);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return dexterityEffects;
//            }
//        }
//        FileConfiguration trait = YamlConfiguration.loadConfiguration(dropsFile);
//        try {
//            dexterityEffects[0] = trait.getDouble("traitList.dexterity.effects.artefact-discovery-progress", 0.0);
//            dexterityEffects[1] = trait.getDouble("traitList.dexterity.effects.double-adp", 0.0);
//            dexterityEffects[2] = trait.getDouble("traitList.dexterity.effects.gain-adp", 0.0);
//            dexterityEffects[3] = trait.getDouble("traitList.dexterity.effects.max-trait-bonus", 0.0);
//        } catch (Exception e) {
//            plugin.getLogger().severe(prefix + "Couldn't load dexterity trait values :" + e.getMessage());
//        }
//        return dexterityEffects;
//    }
}
