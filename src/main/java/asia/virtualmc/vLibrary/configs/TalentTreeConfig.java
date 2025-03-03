package asia.virtualmc.vLibrary.configs;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class TalentTreeConfig {

    public static Map<String, ItemStack> loadTalentsFromFile(@NotNull Plugin plugin) {
        File talentFile = new File(plugin.getDataFolder(), "talent-trees.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(talentFile);
        Map<String, ItemStack> talentItems = new LinkedHashMap<>();

        if (config.isConfigurationSection("talentList")) {
            ConfigurationSection talentSection = config.getConfigurationSection("talentList");
            for (String key : talentSection.getKeys(false)) {
                ConfigurationSection talent = talentSection.getConfigurationSection(key);
                if (talent == null) continue;

                // Get the name and convert it to the required format for the map key
                String displayName = talent.getString("name");
                if (displayName == null) continue;

                // Remove minimessage formatting and convert to lowercase
                String plainName = displayName.replaceAll("<[^>]+>", "")
                        .toLowerCase()
                        .replaceAll("\\s+", "_")
                        .replaceAll("[^a-z0-9_]", "");

                // Create ItemStack with proper material
                String materialStr = talent.getString("material");
                if (materialStr == null) continue;

                Material material = Material.valueOf(materialStr);
                ItemStack itemStack = new ItemStack(material);

                // Set custom model data if present
                int customModelData = talent.getInt("custom-model-data", 0);
                if (customModelData > 0) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        meta.setCustomModelData(customModelData);
                        itemStack.setItemMeta(meta);
                    }
                }

                // Set display name and lore
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(displayName);

                    List<String> lore = talent.getStringList("lore");
                    if (!lore.isEmpty()) {
                        meta.setLore(lore);
                    }

                    itemStack.setItemMeta(meta);
                }

                // Add PDC data if keys are present
                if (talent.isConfigurationSection("keys")) {
                    ConfigurationSection keysSection = talent.getConfigurationSection("keys");
                    if (keysSection != null) {
                        PersistentDataContainer pdc = itemStack.getItemMeta().getPersistentDataContainer();

                        // Process required_level
                        if (keysSection.contains("required_level")) {
                            NamespacedKey requiredLevelKey = new NamespacedKey(plugin, "required_level");
                            pdc.set(requiredLevelKey, PersistentDataType.INTEGER, keysSection.getInt("required_level"));
                        }

                        // Process required_id if it exists
                        if (keysSection.contains("required_id")) {
                            NamespacedKey requiredIdKey = new NamespacedKey(plugin, "required_id");
                            String requiredIdStr = keysSection.getString("required_id");
                            if (requiredIdStr != null && !requiredIdStr.isEmpty()) {
                                String[] requiredIdParts = requiredIdStr.split(",\\s*");
                                int[] requiredIds = new int[requiredIdParts.length];
                                for (int i = 0; i < requiredIdParts.length; i++) {
                                    requiredIds[i] = Integer.parseInt(requiredIdParts[i].trim());
                                }
                                pdc.set(requiredIdKey, PersistentDataType.INTEGER_ARRAY, requiredIds);
                            }
                        }

                        // Process integer_<number> and double_<number> keys
                        for (String pdcKey : keysSection.getKeys(false)) {
                            if (pdcKey.startsWith("integer_")) {
                                NamespacedKey intKey = new NamespacedKey(plugin, pdcKey);
                                pdc.set(intKey, PersistentDataType.INTEGER, keysSection.getInt(pdcKey));
                            } else if (pdcKey.startsWith("double_")) {
                                NamespacedKey doubleKey = new NamespacedKey(plugin, pdcKey);
                                pdc.set(doubleKey, PersistentDataType.DOUBLE, keysSection.getDouble(pdcKey));
                            }
                        }

                        itemStack.setItemMeta(meta);
                    }
                }

                talentItems.put(plainName, itemStack);
            }
        }

        return talentItems;
    }
}
