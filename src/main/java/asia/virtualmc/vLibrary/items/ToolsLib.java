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
        NamespacedKey REQ_LEVEL_KEY = new NamespacedKey(plugin, "required_level");
        NamespacedKey GATHER_KEY = new NamespacedKey(plugin, "gathering_rate");
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

            // Get custom stats from the new path
            double gatherRate = config.getDouble(path + ".custom-stats.gathering-rate", 0.0);
            int reqLevel = config.getInt(path + ".custom-stats.required-level", 1);

            // Get specific stats
            // ARCHAEOLOGY


            // Handle unbreakable flag
            boolean unbreakable = config.getBoolean(path + ".unbreakable", false);

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
            Component nameComponent = miniMessage.deserialize(displayName);
            meta.displayName(nameComponent);

            // Process lore with placeholders before converting with MiniMessage
            List<String> processedLore = new ArrayList<>();
            for (String line : lore) {
                // Replace placeholders with actual values
                String processedLine = line
                        .replace("{required-level}", String.valueOf(reqLevel))
                        .replace("{gathering-rate}", String.valueOf(gatherRate))
                        .replace("{gathering-level}", String.valueOf(gatherRate)) // Handle both possible names
                        .replace("{progress-gain}", String.valueOf(progressGain))
                        .replace("{success-width}", String.valueOf(successWidth))
                        .replace("{ad-bonus}", String.valueOf(adBonus));

                processedLore.add(processedLine);
            }

            // Convert lore using MiniMessage
            List<Component> parsedLore = new ArrayList<>();
            for (String line : lore) {
                Component loreLine = miniMessage.deserialize(line);
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

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(TOOL_KEY, PersistentDataType.INTEGER, itemID);
            container.set(REQ_LEVEL_KEY, PersistentDataType.INTEGER, reqLevel);
            container.set(GATHER_KEY, PersistentDataType.DOUBLE, gatherRate);

            if (plugin.getName().equals("vArchaeology")) {
                NamespacedKey ADP_RATE = new NamespacedKey(plugin, "adp_rate");
                container.set(ADP_RATE, PersistentDataType.DOUBLE, config.getDouble(path + ".custom-stats.ad-bonus", 0.0));
            } else if (plugin.getName().equals("vFishing")) {
                NamespacedKey SUCCESS_WIDTH = new NamespacedKey(plugin, "success_width");
                NamespacedKey PROGRESS_GAIN = new NamespacedKey(plugin, "progress_gain");
                container.set(SUCCESS_WIDTH, PersistentDataType.INTEGER, config.getInt(path + ".custom-stats.success-width", 0));
                container.set(PROGRESS_GAIN, PersistentDataType.INTEGER, config.getInt(path + ".custom-stats.progress-gain", 0));
            }

            item.setItemMeta(meta);
            toolCache.put(toolName, item.clone());
            itemID++;
        }

        return toolCache;
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
