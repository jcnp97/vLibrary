package asia.virtualmc.vLibrary.utils;

import asia.virtualmc.vLibrary.items.ItemsLib;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InventoryUtils {

    public static Map<Integer, ItemStack> createSnapshot(Player player, NamespacedKey ITEM_KEY) {
        Map<Integer, ItemStack> snapshot = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || !item.hasItemMeta()) continue;

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (pdc.has(ITEM_KEY)) {
                snapshot.put(i, item.clone());
            }
        }

        return snapshot;
    }

    public static Map<Integer, ItemStack> createSnapshot(Player player) {
        Map<Integer, ItemStack> snapshot = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;

            snapshot.put(i, item.clone());
        }

        return snapshot;
    }

    public static boolean compareSnapshot(Player player, Map<Integer, ItemStack> snapshot) {
        for (Map.Entry<Integer, ItemStack> entry : snapshot.entrySet()) {
            ItemStack current = player.getInventory().getItem(entry.getKey());
            ItemStack snapshotItem = entry.getValue();

            if (current == null || !current.equals(snapshotItem)) {
                return false;
            }
        }
        return true;
    }

    // Map<ItemID, Amount>
    public static Map<Integer, ItemStack> getInventoryItems(Player player, NamespacedKey ITEM_KEY, Set<Integer> itemIDs) {
        Map<Integer, ItemStack> inventoryItems = new HashMap<>();

        for (ItemStack item : player.getInventory()) {
            int itemID = ItemsLib.getItemID(item, ITEM_KEY);
            if (itemID == 0 || !itemIDs.contains(itemID)) continue;

            inventoryItems.merge(itemID, item.getAmount(), Integer::sum);
        }

        return inventoryItems;
    }

    // Map<Material, Amount>
    public static Map<Material, Integer> getInventoryItems(Player player, Set<Material> materials) {
        Map<Material, Integer> inventoryItems = new HashMap<>();

        for (ItemStack item : player.getInventory()) {
            if (materials.contains(item.getType())) {
                inventoryItems.merge(item.getType(), item.getAmount(), Integer::sum);
            }
        }

        return inventoryItems;
    }


}
