package asia.virtualmc.vLibrary.configs;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.*;

public class TalentTreeConfig {
    public record Talents(String talentName, int requiredLevel, Optional<List<Integer>> requiredID,
                          int[] values, ItemStack item) {}

    public static Map<Integer, Talents> loadTalentsFromFile(@NotNull Plugin plugin) {
        File talentFile = new File(plugin.getDataFolder(), "talent-trees.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(talentFile);
        Map<Integer, Talents> talents = new LinkedHashMap<>();

        if (config.isConfigurationSection("talentList")) {
            ConfigurationSection talentListSection = config.getConfigurationSection("talentList");
            if (talentListSection != null) {
                for (String key : talentListSection.getKeys(false)) {
                    ConfigurationSection talentSection = talentListSection.getConfigurationSection(key);
                    if (talentSection == null) continue;

                    int talentId;
                    try {
                        talentId = Integer.parseInt(key);
                    } catch (NumberFormatException e) {
                        continue; // Skip if not a valid number
                    }

                    // Get the name and convert it to the required format
                    String displayName = talentSection.getString("name");
                    if (displayName == null) continue;

                    // Remove minimessage formatting and convert to lowercase
                    String talentName = displayName.replaceAll("<[^>]+>", "")
                            .toLowerCase()
                            .replaceAll("\\s+", "_")
                            .replaceAll("[^a-z0-9_]", "");

                    // Get required level
                    int requiredLevel = talentSection.getInt("required_level", 0);

                    // Get required ID if exists
                    Optional<List<Integer>> requiredID = Optional.empty();
                    if (talentSection.contains("required_id")) {
                        String requiredIdStr = talentSection.getString("required_id");
                        if (requiredIdStr != null && !requiredIdStr.isEmpty()) {
                            List<Integer> requiredIds = new ArrayList<>();
                            String[] requiredIdParts = requiredIdStr.split(",\\s*");
                            for (String idPart : requiredIdParts) {
                                try {
                                    requiredIds.add(Integer.parseInt(idPart.trim()));
                                } catch (NumberFormatException ignored) {
                                    // Skip invalid numbers
                                }
                            }
                            if (!requiredIds.isEmpty()) {
                                requiredID = Optional.of(requiredIds);
                            }
                        }
                    }

                    // Get values
                    ConfigurationSection valuesSection = talentSection.getConfigurationSection("values");
                    int[] values = new int[0];
                    if (valuesSection != null) {
                        Set<String> valueKeys = valuesSection.getKeys(false);
                        values = new int[valueKeys.size()];
                        int index = 0;
                        for (String valueKey : valueKeys) {
                            values[index++] = valuesSection.getInt(valueKey);
                        }
                    }

                    // Create ItemStack with proper material
                    String materialStr = talentSection.getString("material");
                    if (materialStr == null) continue;

                    Material material;
                    try {
                        material = Material.valueOf(materialStr);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }

                    ItemStack itemStack = new ItemStack(material);

                    // Set ItemMeta properties
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta != null) {
                        // Set display name
                        meta.displayName(MiniMessage.miniMessage().deserialize(displayName));

                        // Set custom model data if present
                        int customModelData = talentSection.getInt("custom-model-data", 0);
                        if (customModelData > 0) {
                            meta.setCustomModelData(customModelData);
                        }

                        // Set lore if present
                        List<String> lore = talentSection.getStringList("lore");
                        if (!lore.isEmpty()) {
                            List<Component> formattedLore = new ArrayList<>();
                            for (String line : lore) {
                                formattedLore.add(MiniMessage.miniMessage().deserialize(line));
                            }
                            meta.lore(formattedLore);
                        }


                        itemStack.setItemMeta(meta);
                    }

                    Talents talent = new Talents(talentName, requiredLevel, requiredID, values, itemStack);
                    talents.put(talentId, talent);
                }
            }
        }

        return talents;
    }
}