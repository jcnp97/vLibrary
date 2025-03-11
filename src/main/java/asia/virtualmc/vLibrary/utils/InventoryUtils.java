package asia.virtualmc.vLibrary.utils;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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

    public static Map<Integer, ItemStack> createSnapshot(Player player, NamespacedKey ITEM_KEY, Set<Integer> itemIDs) {
        Map<Integer, ItemStack> snapshot = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null || !item.hasItemMeta()) continue;

            PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
            if (itemIDs.contains(pdc.getOrDefault(ITEM_KEY, PersistentDataType.INTEGER, 0))) {
                snapshot.put(i, item.clone());
            }
        }

        return snapshot;
    }

    public static Map<Integer, ItemStack> createSnapshot(Player player, Set<Material> materials) {
        Map<Integer, ItemStack> snapshot = new HashMap<>();

        for (int i = 0; i < 36; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;

            if (materials.contains(item.getType())) {
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
}
