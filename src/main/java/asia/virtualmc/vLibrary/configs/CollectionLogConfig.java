package asia.virtualmc.vLibrary.configs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
import java.util.logging.Level;

public class CollectionLogConfig {

    // returns a list of all collection names for database
    public static List<String> loadCLFileForDatabase(@NotNull Plugin plugin) {
        File collectionFile = new File(plugin.getDataFolder(), "collection-log.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(collectionFile);
        List<String> collectionList = new ArrayList<>();

        ConfigurationSection collectionSection = config.getConfigurationSection("collectionList");
        if (collectionSection == null) {
            return collectionList;
        }

        ConfigurationSection byRaritySection = collectionSection.getConfigurationSection("by-rarity");
        if (byRaritySection != null) {
            List<String> rarityOrder = Arrays.asList("common", "uncommon", "rare", "unique", "special", "mythical", "exotic");
            for (String rarity : rarityOrder) {
                if (byRaritySection.isList(rarity)) {
                    collectionList.addAll(byRaritySection.getStringList(rarity));
                }
            }
        } else if (collectionSection.isList("by-rarity")) {
            collectionList.addAll(collectionSection.getStringList("by-rarity"));
        }

        ConfigurationSection groupedSection = collectionSection.getConfigurationSection("by-group");
        if (groupedSection != null) {
            List<Integer> sortedKeys = new ArrayList<>();
            for (String key : groupedSection.getKeys(false)) {
                try {
                    sortedKeys.add(Integer.parseInt(key));
                } catch (NumberFormatException e) {
                    // Ignore keys that are not integers.
                }
            }
            Collections.sort(sortedKeys);
            for (Integer key : sortedKeys) {
                String keyStr = String.valueOf(key);
                if (groupedSection.isList(keyStr)) {
                    collectionList.addAll(groupedSection.getStringList(keyStr));
                }
            }
        }
        return collectionList;
    }

    // returns ItemStack map of collection-log.yml for GUI item display
    public static Map<Integer, ItemStack> loadCLFileForGUI(@NotNull Plugin plugin,
                                                           @NotNull String ITEM_FILE,
                                                           String prefix) {

        Map<Integer, ItemStack> collectionLogCache = new LinkedHashMap<>();
        File itemFile = new File(plugin.getDataFolder(), ITEM_FILE);
        FileConfiguration config;

        try {
            if (!itemFile.exists()) {
                itemFile.getParentFile().mkdirs();
                plugin.saveResource(ITEM_FILE, false);
            }
            config = YamlConfiguration.loadConfiguration(itemFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, prefix + "Failed to create items file on " + ITEM_FILE, e);
            return collectionLogCache;
        }

        // Load global settings.
        ConfigurationSection globalSettings = config.getConfigurationSection("globalSettings");
        if (globalSettings == null) {
            plugin.getLogger().severe(prefix + "No global settings found in " + ITEM_FILE);
            return collectionLogCache;
        }

        String globalMaterial = globalSettings.getString("material", "FLINT");
        int startingModelData = globalSettings.getInt("starting-model-data", 100000);
        int currentId = globalSettings.getInt("starting-id", 1);

        Material material;
        try {
            material = Material.valueOf(globalMaterial.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe(prefix + "Invalid material: " + globalMaterial + " defaulting to FLINT.");
            material = Material.FLINT;
        }

        // Load lore configurations.
        // Note: In the new file, rarity lore is stored under "rarity-lore" and group lore under "group-lore".
        ConfigurationSection rarityLoreSection = globalSettings.getConfigurationSection("rarity-lore");
        ConfigurationSection groupLoreSection = globalSettings.getConfigurationSection("group-lore");

        // Get the collection list (instead of dropsList) from the file.
        ConfigurationSection collectionListSection = config.getConfigurationSection("collectionList");
        if (collectionListSection == null) {
            plugin.getLogger().severe(prefix + "No collectionList found in " + ITEM_FILE);
            return collectionLogCache;
        }

        int currentModelData = startingModelData;
        MiniMessage miniMessage = MiniMessage.miniMessage();

        // Process each group in collectionList.
        List<Integer> groupIDs = new ArrayList<>();
        for (String groupKey : collectionListSection.getKeys(false)) {
            try {
                groupIDs.add(Integer.parseInt(groupKey));
            } catch (NumberFormatException e) {
                plugin.getLogger().warning(prefix + "Invalid group id in collectionList: " + groupKey);
            }
        }
        Collections.sort(groupIDs);

        for (Integer groupID : groupIDs) {
            ConfigurationSection groupSection = collectionListSection.getConfigurationSection(String.valueOf(groupID));
            if (groupSection == null) {
                continue;
            }

            // Process each rarity within the current group.
            List<Integer> rarityIDs = new ArrayList<>();
            for (String rarityKey : groupSection.getKeys(false)) {
                try {
                    rarityIDs.add(Integer.parseInt(rarityKey));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning(prefix + "Invalid rarity id in collectionList for group " + groupID + ": " + rarityKey);
                }
            }
            Collections.sort(rarityIDs);

            for (Integer rarityID : rarityIDs) {
                List<String> itemNames = groupSection.getStringList(String.valueOf(rarityID));
                if (itemNames.isEmpty()) {
                    continue;
                }

                for (String itemName : itemNames) {
                    try {
                        // Create the base item using the global material.
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            // Set the display name (using MiniMessage to deserialize colors and formatting).
                            Component displayName = miniMessage.deserialize("<!i>" + itemName);
                            meta.displayName(displayName);

                            List<Component> finalLore = new ArrayList<>();

                            // If rarityID is non-zero, add rarity lore.
                            if (rarityID != 0 && rarityLoreSection != null) {
                                List<String> rarityLoreLines = rarityLoreSection.getStringList(String.valueOf(rarityID));
                                for (String line : rarityLoreLines) {
                                    finalLore.add(miniMessage.deserialize("<!i>" + line));
                                }
                            }
                            // If groupID is non-zero, add group lore.
                            if (groupID != 0 && groupLoreSection != null) {
                                List<String> groupLoreLines = groupLoreSection.getStringList(String.valueOf(groupID));
                                for (String line : groupLoreLines) {
                                    finalLore.add(miniMessage.deserialize("<!i>" + line));
                                }
                            }

                            // Set lore if any was added.
                            if (!finalLore.isEmpty()) {
                                meta.lore(finalLore);
                            }

                            // Set the custom model data.
                            meta.setCustomModelData(currentModelData);
                            item.setItemMeta(meta);
                        }

                        // Add the item to the drop cache.
                        collectionLogCache.put(currentId, item);

                        // Increment counters for the next item.
                        currentId++;
                        currentModelData++;
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().log(Level.SEVERE, prefix + "Failed to create item: " + itemName, e);
                    }
                }
            }
        }

        return collectionLogCache;
    }

    // returns ItemStack map of collections.yml
    public static Map<String, ItemStack> loadCLFileForItem(@NotNull Plugin plugin,
                                                           @NotNull String ITEM_FILE,
                                                           @NotNull NamespacedKey ITEM_KEY,
                                                           String prefix) {

        Map<String, ItemStack> collectionLogCache = new LinkedHashMap<>();
        File itemFile = new File(plugin.getDataFolder(), ITEM_FILE);
        FileConfiguration config;

        try {
            if (!itemFile.exists()) {
                itemFile.getParentFile().mkdirs();
                plugin.saveResource(ITEM_FILE, false);
            }
            config = YamlConfiguration.loadConfiguration(itemFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, prefix + "Failed to create items file on " + ITEM_FILE, e);
            return collectionLogCache;
        }

        // Load global settings.
        ConfigurationSection globalSettings = config.getConfigurationSection("globalSettings");
        if (globalSettings == null) {
            plugin.getLogger().severe(prefix + "No global settings found in " + ITEM_FILE);
            return collectionLogCache;
        }

        String globalMaterial = globalSettings.getString("material", "FLINT");
        int startingModelData = globalSettings.getInt("starting-model-data", 100000);
        int currentId = globalSettings.getInt("starting-id", 1);

        Material material;
        try {
            material = Material.valueOf(globalMaterial.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe(prefix + "Invalid material: " + globalMaterial + " defaulting to FLINT.");
            material = Material.FLINT;
        }

        // Load lore configurations.
        // Note: In the new file, rarity lore is stored under "rarity-lore" and group lore under "group-lore".
        ConfigurationSection rarityLoreSection = globalSettings.getConfigurationSection("rarity-lore");
        ConfigurationSection groupLoreSection = globalSettings.getConfigurationSection("group-lore");

        // Get the collection list (instead of dropsList) from the file.
        ConfigurationSection collectionListSection = config.getConfigurationSection("collectionList");
        if (collectionListSection == null) {
            plugin.getLogger().severe(prefix + "No collectionList found in " + ITEM_FILE);
            return collectionLogCache;
        }

        int currentModelData = startingModelData;
        MiniMessage miniMessage = MiniMessage.miniMessage();

        // Process each group in collectionList.
        List<Integer> groupIDs = new ArrayList<>();
        for (String groupKey : collectionListSection.getKeys(false)) {
            try {
                groupIDs.add(Integer.parseInt(groupKey));
            } catch (NumberFormatException e) {
                plugin.getLogger().warning(prefix + "Invalid group id in collectionList: " + groupKey);
            }
        }
        Collections.sort(groupIDs);

        for (Integer groupID : groupIDs) {
            ConfigurationSection groupSection = collectionListSection.getConfigurationSection(String.valueOf(groupID));
            if (groupSection == null) {
                continue;
            }

            // Process each rarity within the current group.
            List<Integer> rarityIDs = new ArrayList<>();
            for (String rarityKey : groupSection.getKeys(false)) {
                try {
                    rarityIDs.add(Integer.parseInt(rarityKey));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning(prefix + "Invalid rarity id in collectionList for group " + groupID + ": " + rarityKey);
                }
            }
            Collections.sort(rarityIDs);

            for (Integer rarityID : rarityIDs) {
                List<String> itemNames = groupSection.getStringList(String.valueOf(rarityID));
                if (itemNames.isEmpty()) {
                    continue;
                }

                for (String itemName : itemNames) {
                    try {
                        // Create the base item using the global material.
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            // Set the display name (using MiniMessage to deserialize colors and formatting).
                            Component displayName = miniMessage.deserialize("<!i>" + itemName);
                            meta.displayName(displayName);

                            List<Component> finalLore = new ArrayList<>();

                            // If rarityID is non-zero, add rarity lore.
                            if (rarityID != 0 && rarityLoreSection != null) {
                                List<String> rarityLoreLines = rarityLoreSection.getStringList(String.valueOf(rarityID));
                                for (String line : rarityLoreLines) {
                                    finalLore.add(miniMessage.deserialize("<!i>" + line));
                                }
                            }
                            // If groupID is non-zero, add group lore.
                            if (groupID != 0 && groupLoreSection != null) {
                                List<String> groupLoreLines = groupLoreSection.getStringList(String.valueOf(groupID));
                                for (String line : groupLoreLines) {
                                    finalLore.add(miniMessage.deserialize("<!i>" + line));
                                }
                            }

                            // Set lore if any was added.
                            if (!finalLore.isEmpty()) {
                                meta.lore(finalLore);
                            }

                            // Add pdc data
                            PersistentDataContainer container = meta.getPersistentDataContainer();

                            container.set(ITEM_KEY, PersistentDataType.INTEGER, currentId);
                            container.set(new NamespacedKey(plugin, "group_id"), PersistentDataType.INTEGER, groupID);
                            NamespacedKey uniqueKey = new NamespacedKey(plugin, "unique_id");
                            container.set(uniqueKey, PersistentDataType.INTEGER, (int) (Math.random() * Integer.MAX_VALUE));

                            // Set the custom model data.
                            meta.setCustomModelData(currentModelData);
                            item.setItemMeta(meta);
                        }
                        String mapKey = itemName.toLowerCase().replace(' ', '_').replaceAll("'", "");

                        // Add the item to the drop cache.
                        collectionLogCache.put(mapKey, item);

                        // Increment counters for the next item.
                        currentId++;
                        currentModelData++;
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().log(Level.SEVERE, prefix + "Failed to create item: " + itemName, e);
                    }
                }
            }
        }

        return collectionLogCache;
    }
}
