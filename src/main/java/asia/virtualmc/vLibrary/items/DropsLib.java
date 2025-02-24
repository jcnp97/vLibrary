package asia.virtualmc.vLibrary.items;

import asia.virtualmc.vLibrary.global.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class DropsLib {

    public static class DropDetails {
        public int groupID;
        public int rarityID;
        public ItemStack itemStack;

        public DropDetails(int groupID, int rarityID, ItemStack itemStack) {
            this.groupID = groupID;
            this.rarityID = rarityID;
            this.itemStack = itemStack;
        }
    }

    public static Map<String, DropDetails> loadDropsFromFile(@NotNull Plugin plugin,
                                                             @NotNull String ITEM_FILE,
                                                             @NotNull NamespacedKey ITEM_KEY,
                                                             String prefix,
                                                             boolean isUnique) {
        // Use a LinkedHashMap to preserve insertion order if needed.
        Map<String, DropDetails> dropCache = new LinkedHashMap<>();
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
            return dropCache;
        }

        // Load global settings.
        ConfigurationSection globalSettings = config.getConfigurationSection("globalSettings");
        if (globalSettings == null) {
            plugin.getLogger().severe(prefix + "No global settings found in " + ITEM_FILE);
            return dropCache;
        }

        String globalMaterial = globalSettings.getString("material", "FLINT");
        int startingModelData = globalSettings.getInt("starting-model-data", 100000);

        // Load per-rarity lore.
        ConfigurationSection rarityLoreSection = globalSettings.getConfigurationSection("per-rarity-lore");
        if (rarityLoreSection == null) {
            plugin.getLogger().severe(prefix + "No rarity lore configurations found in " + ITEM_FILE);
            return dropCache;
        }

        // Define a fixed ordering for rarities.
        List<String> rarityOrder = Arrays.asList("common", "uncommon", "rare",
                "unique", "special", "mythical", "exotic");

        Material material;
        try {
            material = Material.valueOf(globalMaterial.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().severe(prefix + "Invalid material: " + globalMaterial + " defaulting to FLINT.");
            material = Material.FLINT;
        }

        // Get the drops list from the file.
        ConfigurationSection dropsListSection = config.getConfigurationSection("dropsList");
        if (dropsListSection == null) {
            plugin.getLogger().severe(prefix + "No dropsList found in " + ITEM_FILE);
            return dropCache;
        }

        int currentModelData = startingModelData;
        MiniMessage miniMessage = MiniMessage.miniMessage();

        // initialize ID counter to 1.
        int itemID = 1;

        // Iterate over each group and each rarity in the defined order.
        for (String groupKey : dropsListSection.getKeys(false)) {
            ConfigurationSection groupSection = dropsListSection.getConfigurationSection(groupKey);
            if (groupSection == null) {
                continue; // Skip if the group section is missing.
            }

            int groupID;
            try {
                groupID = Integer.parseInt(groupKey);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning(prefix + "Invalid group id: " + groupKey);
                continue;
            }

            // Process each rarity in the predefined order.
            for (String rarity : rarityOrder) {
                // Get the list of item names for this rarity in the current group.
                List<String> itemNames = groupSection.getStringList(rarity);
                if (itemNames.isEmpty()) {
                    continue; // No items in this rarity; proceed to the next rarity.
                }

                // Get the lore list for this rarity.
                List<String> rarityLore = rarityLoreSection.getStringList(rarity);

                // Determine a rarityID (for example, index+1 so common=1, uncommon=2, etc.)
                int rarityID = rarityOrder.indexOf(rarity) + 1;

                // Process each item name.
                for (String itemName : itemNames) {
                    try {
                        // Create the base item using the global material.
                        ItemStack item = new ItemStack(material);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            // Set the display name (using MiniMessage to deserialize colors and formatting).
                            Component component = miniMessage.deserialize("<!i>" + getRarityColor(rarityID) + itemName);
                            meta.displayName(component);

                            List<Component> coloredLore = new ArrayList<>();
                            for (String line : rarityLore) {
                                coloredLore.add(miniMessage.deserialize("<!i>" + line));
                            }
                            meta.lore(coloredLore);

                            // Set the custom model data.
                            meta.setCustomModelData(currentModelData);

                            // Set persistent data with the current item name.
                            PersistentDataContainer container = meta.getPersistentDataContainer();
                            String formattedItemName = itemName.toLowerCase().replace(' ', '_');
                            container.set(ITEM_KEY, PersistentDataType.INTEGER, itemID);

                            // Add new persistent data for group_id and rarity_id.
                            container.set(new NamespacedKey(plugin, "group_id"), PersistentDataType.INTEGER, groupID);
                            container.set(new NamespacedKey(plugin, "rarity_id"), PersistentDataType.INTEGER, rarityID);

                            // Optionally add a unique persistent key if needed.
                            if (isUnique) {
                                NamespacedKey uniqueKey = new NamespacedKey(plugin, "unique_id");
                                container.set(uniqueKey, PersistentDataType.INTEGER, (int) (Math.random() * Integer.MAX_VALUE));
                            }
                            item.setItemMeta(meta);
                        }

                        // Create the DropDetails object using the group id, rarity id, and the generated item.
                        DropDetails dropDetails = new DropDetails(groupID, rarityID, item.clone());
                        String mapKey = itemName.toLowerCase().replace(' ', '_');
                        dropCache.put(mapKey, dropDetails);

                        // Increment counters for the next item.
                        currentModelData++;
                        itemID++;
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().log(Level.SEVERE, prefix + "Failed to create item: " + itemName, e);
                    }
                }
            }
        }

        return dropCache;
    }

    private static String getRarityColor(int rarityID) {
        switch (rarityID) {
            default -> { return "<green>"; }
            case 2 -> { return "<aqua>"; }
            case 3 -> { return "<dark_aqua>"; }
            case 4 -> { return "<yellow>"; }
            case 5 -> { return "<gold>"; }
            case 6 -> { return "<dark_purple>"; }
            case 7 -> { return "<dark_red>"; }
        }
    }

    public static boolean isCustomItem(@NotNull ItemStack item, @NotNull NamespacedKey ITEM_KEY) {
        if (!item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(ITEM_KEY, PersistentDataType.INTEGER);
    }

    public static boolean giveItem(@NotNull Player player, @NotNull ItemStack item, int amount) {
        int maxStackSize = item.getMaxStackSize();
        List<ItemStack> itemsToGive = new ArrayList<>();

        while (amount > 0) {
            int stackSize = Math.min(amount, maxStackSize);
            amount -= stackSize;
            ItemStack stack = item.clone();
            stack.setAmount(stackSize);
            itemsToGive.add(stack);
        }

        Map<Integer, ItemStack> overflow = player.getInventory().addItem(itemsToGive.toArray(new ItemStack[0]));

        if (!overflow.isEmpty()) {
            for (ItemStack overflowItem : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), overflowItem);
            }
            player.sendMessage(Messages.fullInventoryMessage);
            return false;
        }
        return true;
    }

    public static Map<Integer, ItemStack> checkItemsToRemove(@NotNull Player player,
                                                             @NotNull NamespacedKey ITEM_KEY,
                                                             int rarityID,
                                                             int amount) {
        int remaining = amount;
        Map<Integer, ItemStack> itemsToRemove = new HashMap<>();

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item == null || !item.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (container.has(ITEM_KEY, PersistentDataType.INTEGER)) {
                int storedID = container.getOrDefault(ITEM_KEY, PersistentDataType.INTEGER, 0);
                if (storedID == rarityID) {
                    if (item.getAmount() <= remaining) {
                        itemsToRemove.put(slot, item.clone());
                        remaining -= item.getAmount();
                    } else {
                        ItemStack partial = item.clone();
                        partial.setAmount(remaining);
                        itemsToRemove.put(slot, partial);
                        remaining = 0;
                    }
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
        }
        return remaining > 0 ? null : itemsToRemove;
    }

    public static boolean removeItems(@NotNull Player player, Map<Integer, ItemStack> itemsToRemove) {
        if (itemsToRemove == null) {
            player.sendMessage(Messages.notEnoughItems);
            return false;
        }

        Inventory inventory = player.getInventory();

        for (Map.Entry<Integer, ItemStack> entry : itemsToRemove.entrySet()) {
            int slot = entry.getKey();
            ItemStack reserved = entry.getValue();
            ItemStack current = inventory.getItem(slot);
            if (current == null) {
                continue; // Safety check – ideally this should not happen.
            }
            int removeAmount = reserved.getAmount();
            int currentAmount = current.getAmount();

            if (currentAmount <= removeAmount) {
                // Remove the entire stack.
                inventory.setItem(slot, null);
            } else {
                // Subtract the reserved amount.
                current.setAmount(currentAmount - removeAmount);
                inventory.setItem(slot, current);
            }
        }
        return true;
    }
}
