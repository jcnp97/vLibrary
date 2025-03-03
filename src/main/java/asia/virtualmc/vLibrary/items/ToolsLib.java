package asia.virtualmc.vLibrary.items;

import asia.virtualmc.vLibrary.global.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class ToolsLib {

    public static Map<String, ItemStack> loadToolsFromFile(@NotNull Plugin plugin,
                                                           @NotNull String ITEM_FILE) {

        Map<String, ItemStack> toolCache = new HashMap<>();
        NamespacedKey TOOL_KEY = new NamespacedKey(plugin, "custom_tool");
        String ITEM_SECTION_PATH = "toolsList";

        File configFile = new File(plugin.getDataFolder(), ITEM_FILE);
        try {
            if (!configFile.exists()) {
                configFile.getParentFile().mkdirs();
                plugin.saveResource(ITEM_FILE, false);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create tools on " + ITEM_FILE + ": ", e);
            return toolCache;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        ConfigurationSection toolsSection = config.getConfigurationSection(ITEM_SECTION_PATH);
        if (toolsSection == null) {
            plugin.getLogger().warning("No tools found in " + ITEM_FILE + " under " + ITEM_SECTION_PATH + ".");
            return toolCache;
        }

        MiniMessage miniMessage = MiniMessage.miniMessage();

        // initialize ID counter to 1.
        int itemID = 1;

        for (String toolName : toolsSection.getKeys(false)) {
            String path = ITEM_SECTION_PATH + "." + toolName;

            String materialName = config.getString(path + ".material");
            String displayName = config.getString(path + ".name");
            int customModelData = config.getInt(path + ".custom-model-data", 0);

            // Get stats for placeholder replacement
            Map<String, Double> doubleStats = getCustomStatsDouble(config, path);
            Map<String, Integer> intStats = getCustomStatsInt(config, path);

            // Handle unbreakable flag
            boolean unbreakable = config.getBoolean(path + ".unbreakable", false);

            // Retrieve lore from config
            List<String> lore = config.getStringList(path + ".lore");

            if (materialName == null || displayName == null) {
                plugin.getLogger().warning("Invalid configuration for tool: " + toolName);
                continue;
            }

            ItemStack item;
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                item = new ItemStack(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, "Invalid material '" + materialName + "' for tool: " + toolName, e);
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                plugin.getLogger().warning("Could not retrieve ItemMeta for tool: " + toolName);
                continue;
            }

            // Set display name using MiniMessage
            Component nameComponent = miniMessage.deserialize("<!i>" + displayName);
            meta.displayName(nameComponent);

            // Process lore with placeholders before converting with MiniMessage
            List<String> processedLore = new ArrayList<>();
            for (String line : lore) {
                String processedLine = line;

                // Replace placeholders with integer values
                for (Map.Entry<String, Integer> entry : intStats.entrySet()) {
                    processedLine = processedLine.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                }

                // Replace placeholders with double values
                for (Map.Entry<String, Double> entry : doubleStats.entrySet()) {
                    processedLine = processedLine.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
                }

                processedLore.add(processedLine);
            }

            // Convert processed lore using MiniMessage
            List<Component> parsedLore = new ArrayList<>();
            for (String line : processedLore) {
                Component loreLine = miniMessage.deserialize("<!i>" + line);
                parsedLore.add(loreLine);
            }
            meta.lore(parsedLore);

            meta.setCustomModelData(customModelData);

            // Set unbreakable based on config
            if (unbreakable) {
                meta.setUnbreakable(true);
            }

            // Process enchantments
            List<String> enchantsList = config.getStringList(path + ".enchants");
            meta = addEnchantments(plugin, enchantsList, meta);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            // Set persistent data
            PersistentDataContainer container = meta.getPersistentDataContainer();
            // Set the tool identifier
            container.set(TOOL_KEY, PersistentDataType.INTEGER, itemID);

            // Add all integer stats to PDC
            for (Map.Entry<String, Integer> entry : intStats.entrySet()) {
                NamespacedKey key = new NamespacedKey(plugin, entry.getKey().replace("-", "_"));
                container.set(key, PersistentDataType.INTEGER, entry.getValue());
            }

            // Add all double stats to PDC
            for (Map.Entry<String, Double> entry : doubleStats.entrySet()) {
                NamespacedKey key = new NamespacedKey(plugin, entry.getKey().replace("-", "_"));
                container.set(key, PersistentDataType.DOUBLE, entry.getValue());
            }

            item.setItemMeta(meta);
            toolCache.put(toolName, item.clone());
            itemID++;
        }

        return toolCache;
    }

    private static ItemMeta addEnchantments(Plugin plugin, List<String> enchantsList, ItemMeta meta) {
        String toolName = meta.getDisplayName();

        for (String enchantEntry : enchantsList) {
            String[] parts = enchantEntry.split(":");
            if (parts.length == 2) {
                String enchantName = parts[0];
                int level;
                try {
                    level = Integer.parseInt(parts[1]);
                    Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                    if (enchant != null) {
                        meta.addEnchant(enchant, level, true);
                    } else {
                        plugin.getLogger().warning("Invalid enchantment '" + enchantName + "' for tool: " + toolName);
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid enchantment level for '" + enchantEntry + "' in tool: " + toolName);
                }
            } else {
                plugin.getLogger().warning("Invalid enchantment format '" + enchantEntry + "' for tool: " + toolName);
            }
        }

        return meta;
    }

    private static Map<String, Double> getCustomStatsDouble(FileConfiguration config, String path) {
        Map<String, Double> doubleStats = new HashMap<>();

        ConfigurationSection doubleSection = config.getConfigurationSection(path + ".custom-stats.double");
        if (doubleSection != null) {
            for (String stat : doubleSection.getKeys(false)) {
                double value = config.getDouble(path + ".custom-stats.double." + stat);
                doubleStats.put(stat, value);
            }
        }

        return doubleStats;
    }

    private static Map<String, Integer> getCustomStatsInt(FileConfiguration config, String path) {
        Map<String, Integer> intStats = new HashMap<>();

        ConfigurationSection intSection = config.getConfigurationSection(path + ".custom-stats.integer");
        if (intSection != null) {
            for (String stat : intSection.getKeys(false)) {
                int value = config.getInt(path + ".custom-stats.integer." + stat);
                intStats.put(stat, value);
            }
        }

        return intStats;
    }

    public static boolean giveTool(@NotNull Player player, @NotNull ItemStack item) {
        ItemStack giveItem = item.clone();
        giveItem.setAmount(1);
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(giveItem);

        if (!overflow.isEmpty()) {
            for (ItemStack overflowItem : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflowItem);
            }
            player.sendMessage(Messages.fullInventoryMessage);
            return false;
        }
        return true;
    }

    public static boolean takeTool(@NotNull Player player, @NotNull NamespacedKey TOOL_KEY, int id) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (!item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        String toolName = meta.getDisplayName();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (pdc.has(TOOL_KEY, PersistentDataType.INTEGER)) {
            int toolID = pdc.getOrDefault(TOOL_KEY, PersistentDataType.INTEGER, 0);
            if (toolID == id) {
                player.sendMessage("§cYour item: " + toolName + " has been taken from you.");
                HashMap<Integer, ItemStack> remaining = player.getInventory().removeItem(item);
                return remaining.isEmpty();
            }
        }
        return false;
    }

    public static boolean isCustomTool(@NotNull ItemStack item, @NotNull NamespacedKey TOOL_KEY) {
        if (!item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(TOOL_KEY, PersistentDataType.INTEGER);
    }

    public static double getToolGatherRate(ItemStack item, @NotNull NamespacedKey GATHER_KEY) {
        if (item.getItemMeta() == null) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        return pdc.getOrDefault(GATHER_KEY, PersistentDataType.DOUBLE, 0.0);
    }

    public static double getToolDataDouble(ItemStack item, @NotNull NamespacedKey KEY) {
        if (item.getItemMeta() == null) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        return pdc.getOrDefault(KEY, PersistentDataType.DOUBLE, 0.0);
    }

    public static int getToolDataInt(ItemStack item, @NotNull NamespacedKey KEY) {
        if (item.getItemMeta() == null) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();

        return pdc.getOrDefault(KEY, PersistentDataType.INTEGER, 0);
    }

    public static boolean compareTool(@NotNull ItemStack item, @NotNull NamespacedKey TOOL_KEY, int toolID) {
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(TOOL_KEY, PersistentDataType.INTEGER)) {
            return pdc.getOrDefault(TOOL_KEY, PersistentDataType.INTEGER, 0) == toolID;
        }
        return false;
    }

    public static int getToolLevel(@NotNull ItemStack item, @NotNull NamespacedKey REQ_LEVEL_KEY) {
        if (!item.hasItemMeta()) return 0;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Integer id = pdc.get(REQ_LEVEL_KEY, PersistentDataType.INTEGER);
        return id != null ? id : 0;
    }

    public static Integer getDurability(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        if (!(item.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable itemMeta)) {
            return null;
        }
        int maxDurability = item.getType().getMaxDurability();

        if (maxDurability == 0) {
            return null;
        }
        return maxDurability - itemMeta.getDamage();
    }
}
