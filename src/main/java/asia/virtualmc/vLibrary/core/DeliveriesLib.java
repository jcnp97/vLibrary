package asia.virtualmc.vLibrary.core;

import asia.virtualmc.vLibrary.configs.GUIConfig;
import asia.virtualmc.vLibrary.guis.GUILib;
import asia.virtualmc.vLibrary.items.ItemsLib;
import asia.virtualmc.vLibrary.items.ResourcesLib;
import asia.virtualmc.vLibrary.utils.DigitUtils;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.*;

public class DeliveriesLib {
    private static final Random random = new Random();

    public static List<ItemStack> getVanillaItems(int min, int max) {
        int[] range = validateRange(min, max);

        int count = random.nextInt(range[0], range[1] + 1);
        List<ItemStack> items = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Material material = getRandomMaterial();
            int quantity = random.nextInt(1, 256);
            ItemStack item = new ItemStack(material);

            while (quantity > 0) {
                int amount = Math.min(64, quantity);
                ItemStack stack = item.clone();
                stack.setAmount(amount);
                items.add(stack);
                quantity -= amount;
            }
        }

        return items;
    }

    public static int[] validateRange(int min, int max) {
        if (min > max) {
            int temp = min;
            min = max;
            max = temp;
        } else if (min == max) {
            min -= 1;
        }

        return new int[]{min, max};
    }

    public static Material getRandomMaterial() {
        Material[] materials = {Material.COD, Material.PUFFERFISH, Material.TROPICAL_FISH};
        return materials[random.nextInt(materials.length)];
    }

    public static List<Integer> getRarityIDs(int level) {
        List<Integer> rarityIDs = new ArrayList<>();
        rarityIDs.add(1);
        if (level >= 10) rarityIDs.add(2);
        if (level >= 20) rarityIDs.add(3);
        if (level >= 30) rarityIDs.add(4);
        if (level >= 40) rarityIDs.add(5);
        if (level >= 50) rarityIDs.add(6);
        if (level >= 60) rarityIDs.add(7);

        return rarityIDs;
    }

    public static List<Integer> getRegionIDs(int level) {
        List<Integer> regionIDs = new ArrayList<>();
        regionIDs.add(1);
        regionIDs.add(2);
        if (level >= 10) regionIDs.add(3);
        if (level >= 20) regionIDs.add(4);
        if (level >= 30) regionIDs.add(5);
        if (level >= 40) regionIDs.add(6);
        if (level >= 50) regionIDs.add(7);
        if (level >= 60) regionIDs.add(8);

        return regionIDs;
    }

    public static int getMaxTasks(int level) {
        int max = 6;
        if (level >= 10) max += 1;
        if (level >= 20) max += 1;
        if (level >= 30) max += 1;
        if (level >= 40) max += 1;
        if (level >= 50) max += 1;
        if (level >= 60) max += 1;

        return max;
    }

    public static int getRandomID(List<Integer> numbers) {
        return numbers.get(random.nextInt(numbers.size()));
    }

    public static List<String> getDeliveryLore(List<ItemStack> items, long duration) {
        List<String> lore = new ArrayList<>();
        lore.add("§8§m                                        ");

        for (ItemStack item : items) {
            lore.add("§7• " + ItemsLib.getDisplayName(item) + " §x" + ItemsLib.getItemStackAmount(item));
        }
        lore.add("§8§m                                        ");
        lore.add("§7Expires in: §c" + DigitUtils.formatDuration(duration));

        return lore;
    }

    public static ItemStack createTaskButton(int taskID, long expiration, List<ItemStack> items, boolean completed) {
        long currentTime = Instant.now().getEpochSecond();

        if (completed) {
            List<String> lore = List.of(
                    "§7Refreshes in: §e" + DigitUtils.formatDuration(expiration - currentTime)
            );

            return GUILib.createLegacyButton(Material.BARRIER,
                    "§aDelivery Completed!", 1, lore);
        }

        String displayName = "§eDelivery #§a" + taskID;
        List<String> lore = getDeliveryLore(items, expiration - currentTime);

        return GUILib.createLegacyButton(Material.EMERALD,
                displayName, 1, lore);
    }

    public static OutlinePane createCustomItemDisplay(Player player, List<ItemStack> items, NamespacedKey ITEM_KEY) {
        OutlinePane pane = new OutlinePane(0, 0, 9, 3);

        Set<Integer> itemIDs = getAllIDs(items, ITEM_KEY);
        Map<Integer, Integer> inventoryItems = getInventoryItems(player, itemIDs, ITEM_KEY);

        for (ItemStack item : items) {
            int itemID = ItemsLib.getItemID(item, ITEM_KEY);

            if (inventoryItems.get(itemID) >= item.getAmount()) {
                inventoryItems.merge(itemID, -item.getAmount(), Integer::sum);
                pane.addItem(new GuiItem(item));
            } else {
                ItemStack newItem = cloneItem(Material.BARRIER, item);
                pane.addItem(new GuiItem(newItem));
            }
        }

        return pane;
    }

    public static Set<Integer> getAllIDs(List<ItemStack> items, NamespacedKey ITEM_KEY) {
        Set<Integer> itemIDs = new HashSet<>();

        for (ItemStack item : items) {
            int itemID = ItemsLib.getItemID(item, ITEM_KEY);
            itemIDs.add(itemID);
        }

        return itemIDs;
    }

    public static boolean compareCustomItems(Player player, List<ItemStack> items, NamespacedKey ITEM_KEY) {
        Set<Integer> itemIDs = getAllIDs(items, ITEM_KEY);
        Map<Integer, Integer> inventoryItems = getInventoryItems(player, itemIDs, ITEM_KEY);

        for (ItemStack item : items) {
            int itemID = ItemsLib.getItemID(item, ITEM_KEY);

            if (inventoryItems.get(itemID) >= item.getAmount()) {
                inventoryItems.merge(itemID, -item.getAmount(), Integer::sum);
            } else {
                return false;
            }
        }

        return true;
    }

    // For Vanilla Items
    public static OutlinePane createVanillaItemDisplay(Player player, List<ItemStack> items) {
        OutlinePane pane = new OutlinePane(0, 0, 9, 3);

        Set<Material> materials = getMaterials(items);
        Map<Material, Integer> inventoryItems = getInventoryItems(player, materials);

        for (ItemStack item : items) {
            Material material = item.getType();

            if (inventoryItems.get(material) >= item.getAmount()) {
                inventoryItems.merge(material, -item.getAmount(), Integer::sum);
                pane.addItem(new GuiItem(item));
            } else {
                ItemStack newItem = cloneItem(Material.BARRIER, item);
                pane.addItem(new GuiItem(newItem));
            }
        }

        return pane;
    }



    public static Set<Material> getMaterials(List<ItemStack> items) {
        Set<Material> materials = new HashSet<>();

        for (ItemStack item : items) {
            materials.add(item.getType());
        }

        return materials;
    }

    public static ItemStack cloneItem(Material material, ItemStack item) {
        ItemStack newItem = new ItemStack(material, item.getAmount());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta == null) return newItem;

            ItemMeta newMeta = newItem.getItemMeta();
            if (newMeta == null) return newItem;

            newMeta.setDisplayName(meta.getDisplayName());
            newMeta.setLore(meta.getLore());
            newMeta.setCustomModelData(meta.getCustomModelData());

            newItem.setItemMeta(newMeta);
        }

        return newItem;
    }
}
