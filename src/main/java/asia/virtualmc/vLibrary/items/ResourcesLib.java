package asia.virtualmc.vLibrary.items;

import asia.virtualmc.vLibrary.utils.EffectsUtil;
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
import java.util.stream.Collectors;

public class ResourcesLib {

    public static class ResourceDetails {
        public int numericalID;
        public int rarityID;
        public int regionID;
        public ItemStack itemStack;

        public ResourceDetails(int numericalID, int rarityID, int regionID, ItemStack itemStack) {
            this.numericalID = numericalID;
            this.rarityID = rarityID;
            this.regionID = regionID;
            this.itemStack = itemStack;
        }
    }

    public static Map<String, ResourceDetails> readFromFile(
            @NotNull Plugin plugin,
            @NotNull String FILE_DIR,
            @NotNull NamespacedKey ITEM_KEY) {

        Map<String, ResourceDetails> resourceCache = new LinkedHashMap<>();
        String SETTINGS_FILE = FILE_DIR + "settings.yml";

        File settingsFile = new File(plugin.getDataFolder(), SETTINGS_FILE);
        FileConfiguration settingsConfig;

        // Check if settings.yml exists
        try {
            if (!settingsFile.exists()) {
                settingsFile.getParentFile().mkdirs();
                plugin.saveResource(SETTINGS_FILE, false);
            }
            settingsConfig = YamlConfiguration.loadConfiguration(settingsFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to read: " + SETTINGS_FILE, e);
            return resourceCache;
        }

        // Load global settings.
        ConfigurationSection globalSettings = settingsConfig.getConfigurationSection("globalSettings");
        if (globalSettings == null) {
            plugin.getLogger().severe("No global settings found in " + SETTINGS_FILE);
            return resourceCache;
        }

        String globalMaterial = globalSettings.getString("material", "PAPER");
        int currentModelData = globalSettings.getInt("starting-model-data", 1);
        List<String> rarityIDToName = globalSettings.getStringList("rarity-id-to-name");
        List<String> rarityColor = globalSettings.getStringList("rarity-color");
        List<String> regionIDToName = globalSettings.getStringList("region-id-to-name");
        String lineDivider = globalSettings.getString("lore-settings.line-divider");
        String loreFormatting = globalSettings.getString("lore-settings.formatting");

        // Load read material from STRING to MATERIAL
        Material material;
        try {
            material = Material.valueOf(globalMaterial.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe("Invalid material: " + globalMaterial + " defaulting to PAPER.");
            material = Material.PAPER;
        }

        // Store placeholders into a map to be used when adding displayNames and lore.
        Map<String, String> placeholdersMap = new HashMap<>();
        if (globalSettings.getConfigurationSection("placeholders") != null) {
            for (String key : globalSettings.getConfigurationSection("placeholders").getKeys(false)) {
                String value = globalSettings.getString("placeholders." + key);
                if (value != null) {
                    placeholdersMap.put(key, value);
                }
            }
        }

        // Define a fixed ordering for rarities.
        List<String> rarityOrder = Arrays.asList("common", "uncommon", "rare",
                "unique", "special", "mythical", "exotic");

        // Setup itemID and minimessage.
        int itemID = 1;
        MiniMessage miniMessage = MiniMessage.miniMessage();

        // Process each rarity file if it exists
        for (String rarityFile : rarityOrder) {
            File file = new File(plugin.getDataFolder(), FILE_DIR + rarityFile + ".yml");

            // Skip if file doesn't exist but log a warning
            if (!file.exists()) {
                plugin.getLogger().severe("Rarity file not found: " + rarityFile + ".yml - Skipping this rarity level.");
                continue;
            }

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            ConfigurationSection contentSection = config.getConfigurationSection("contentList");

            // Skip if contentList section doesn't exist
            if (contentSection == null) {
                plugin.getLogger().severe("No contentList section found in " + rarityFile + ".yml - Skipping this rarity level.");
                continue;
            }

            int rarityID = rarityOrder.indexOf(rarityFile) + 1;

            for (String itemName : contentSection.getKeys(false)) {
                // Check if fishName already exists in the map (new feature)
                if (resourceCache.containsKey(itemName)) {
                    plugin.getLogger().severe("Duplicate fish name '" + itemName + "' found in " + rarityFile + ".yml - Skipping this item.");
                    continue;
                }

                // Get the item-specific section
                ConfigurationSection itemSection = contentSection.getConfigurationSection(itemName);

                if (itemSection == null) {
                    plugin.getLogger().warning("Invalid item section for " + itemName + " in " + rarityFile + ".yml - Skipping this item.");
                    continue;
                }

                // Get optional parameters with defaults
                String displayName = itemSection.getString("display-name");

                // Retrieve lore from config
                List<String> configLore = EffectsUtil.divideLore(itemSection.getStringList("lore"), 36);

                // Region ID is required
                if (!itemSection.contains("region-id")) {
                    plugin.getLogger().warning("Missing region-id for " + itemName + " in " + rarityFile + ".yml - Skipping this item.");
                    continue;
                }
                int regionID = itemSection.getInt("region-id");

                // Process item name if displayName not provided
                if (displayName == null) {
                    displayName = capitalizeWords(itemName.replace("_", " "));
                }

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    // Set displayName (optional)
                    if (displayName != null) {
                        Component component = miniMessage.deserialize("<!i>" +
                                rarityColor.get(Math.min(rarityID - 1, rarityColor.size() - 1)) + displayName);
                        meta.displayName(component);
                    }

                    // Set lore (optional)
                    List<Component> loreComponents = new ArrayList<>();

                    // Add rarity and region info
                    if (rarityID <= rarityIDToName.size()) {
                        loreComponents.add(miniMessage.deserialize("<!i>" + rarityIDToName.get(rarityID - 1)));
                    }

                    if (regionID <= regionIDToName.size() && regionID > 0) {
                        loreComponents.add(miniMessage.deserialize("<!i>" + regionIDToName.get(regionID - 1)));
                    }

                    // Add separator
                    loreComponents.add(miniMessage.deserialize(lineDivider));

                    // Add item-specific lore if provided
                    if (configLore != null && !configLore.isEmpty()) {
                        for (String loreLine : configLore) {
                            loreComponents.add(miniMessage.deserialize(loreFormatting + loreLine));
                        }
                        // Add closing separator after lore
                        loreComponents.add(miniMessage.deserialize(lineDivider));
                    }

                    meta.lore(loreComponents);

                    // Set model data
                    meta.setCustomModelData(currentModelData);
                    currentModelData++;

                    // Add PDC data
                    PersistentDataContainer pdc = meta.getPersistentDataContainer();
                    pdc.set(ITEM_KEY, PersistentDataType.INTEGER, itemID);

                    // Add PDC data for rarityID and regionID
                    pdc.set(new NamespacedKey(plugin, "rarity_id"), PersistentDataType.INTEGER, rarityID);
                    pdc.set(new NamespacedKey(plugin, "region_id"), PersistentDataType.INTEGER, regionID);

                    // Add PDC data for weight and quality
                    pdc.set(new NamespacedKey(plugin, "weight"), PersistentDataType.DOUBLE, 0.0);
                    pdc.set(new NamespacedKey(plugin, "quality_id"), PersistentDataType.INTEGER, 0);

                    item.setItemMeta(meta);
                }

                // Create and store the resource details with the current itemID as numericalID
                ResourceDetails record = new ResourceDetails(itemID, rarityID, regionID, item.clone());

                // Store in the resource cache with the itemName as key and record as value
                resourceCache.put(itemName, record);
                itemID++;
            }
        }

        //plugin.getLogger().info(pluginPrefix + "Successfully loaded " + resourceCache.size() + " resources.");
        return resourceCache;
    }

    private static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        return Arrays.stream(str.split("\\s+"))
                .map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public static Map<Integer, Map<Integer, List<String>>> getRarityAndRegionMap(Map<String, ResourceDetails> resourceMap) {
        Map<Integer, Map<Integer, List<String>>> rarityRegionMap = new HashMap<>();

        for (Map.Entry<String, ResourceDetails> entry : resourceMap.entrySet()) {
            String nameId = entry.getKey();
            ResourceDetails details = entry.getValue();
            rarityRegionMap
                    .computeIfAbsent(details.rarityID, k -> new HashMap<>())
                    .computeIfAbsent(details.regionID, k -> new ArrayList<>())
                    .add(nameId);
        }

        return rarityRegionMap;
    }

    public static List<String> getAllNameIDs(Map<String, ResourceDetails> resourceMap) {
        return new ArrayList<>(resourceMap.keySet());
    }

    public static FileConfiguration getConfigurationFile(@NotNull Plugin plugin, String PATH_NAME) {
        File file = new File(plugin.getDataFolder(), PATH_NAME);
        if (!file.exists()) {
            plugin.getLogger().severe("Unable to load configuration file: " + PATH_NAME);
            return null;
        }

        return YamlConfiguration.loadConfiguration(file);
    }

    private static List<String> formatLore(List<String> lore,
                                    boolean autoFormat,
                                    int charCount,
                                    String formatToInclude) {
        if (!autoFormat) {
            return lore;
        }

        List<String> dividedLore = EffectsUtil.divideLore(lore, charCount);
        List<String> newLore = new ArrayList<>();

        for (String line : dividedLore) {
            newLore.add("<gray>" + line);
        }

        return newLore;
    }
}