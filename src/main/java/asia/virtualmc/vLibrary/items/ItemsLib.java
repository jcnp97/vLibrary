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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;

public class ItemsLib {

    public static Map<String, ItemStack> loadItemsFromFile(@NotNull Plugin plugin,
                                                           @NotNull String ITEM_FILE,
                                                           @NotNull NamespacedKey ITEM_KEY,
                                                           String prefix,
                                                           boolean isUnique) {
        Map<String, ItemStack> itemCache = new HashMap<>();
        File itemFile = new File(plugin.getDataFolder(), ITEM_FILE);
        FileConfiguration customItemsConfig;

        try {
            if (!itemFile.exists()) {
                itemFile.getParentFile().mkdirs();
                plugin.saveResource(ITEM_FILE, false);
            }
            customItemsConfig = YamlConfiguration.loadConfiguration(itemFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, prefix + "Failed to create items file on " + ITEM_FILE, e);
            return itemCache;
        }

        // Always use "itemsList" as the section path.
        ConfigurationSection itemsSection = customItemsConfig.getConfigurationSection("itemsList");
        if (itemsSection == null) {
            plugin.getLogger().severe(prefix + "No items found in " + ITEM_FILE + " at path: itemsList");
            return itemCache;
        }

        // initialize ID counter to 1.
        int itemID = 1;

        for (String itemName : itemsSection.getKeys(false)) {
            String path = "itemsList." + itemName;

            try {
                String materialName = customItemsConfig.getString(path + ".material");
                String displayName = customItemsConfig.getString(path + ".name");
                int customModelData = customItemsConfig.getInt(path + ".custom-model-data");
                List<String> lore = customItemsConfig.getStringList(path + ".lore");

                if (materialName == null || displayName == null) {
                    plugin.getLogger().warning(prefix + "Invalid configuration for item: " + itemName);
                    continue;
                }

                Material material = Material.valueOf(materialName.toUpperCase());
                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();

                if (meta != null) {
                    MiniMessage miniMessage = MiniMessage.miniMessage();
                    Component component = miniMessage.deserialize("<!i>" + displayName);
                    meta.displayName(component);

                    List<Component> coloredLore = new ArrayList<>();

                    for (String line : lore) {
                        coloredLore.add(miniMessage.deserialize("<!i>" + line));
                    }

                    meta.lore(coloredLore);

                    meta.setCustomModelData(customModelData);

                    // Store the item name as the key using the provided ITEM_KEY.
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    container.set(ITEM_KEY, PersistentDataType.INTEGER, itemID);

                    if (isUnique) {
                        NamespacedKey unstackableKey = new NamespacedKey(plugin, "unique_id");
                        container.set(unstackableKey, PersistentDataType.INTEGER, (int) (Math.random() * Integer.MAX_VALUE));
                    }

                    item.setItemMeta(meta);
                }

                itemCache.put(itemName, item.clone());
                itemID++;
            } catch (IllegalArgumentException e) {
                plugin.getLogger().log(Level.SEVERE, prefix + "Failed to create item: " + itemName, e);
            }
        }

        return itemCache;
    }

    public static boolean isCustomItem(@NotNull ItemStack item, @NotNull NamespacedKey ITEM_KEY) {
        if (!item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(ITEM_KEY, PersistentDataType.INTEGER);
    }

    public static int getItemID(@NotNull ItemStack item, @NotNull NamespacedKey ITEM_KEY) {
        if (!item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(ITEM_KEY, PersistentDataType.INTEGER, 0);
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
                                                             int itemID,
                                                             int amount) {
        if (itemID <= 0) return null;
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
                Integer storedID = container.get(ITEM_KEY, PersistentDataType.INTEGER);
                if (storedID == null) return null;
                if (storedID == itemID) {
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
